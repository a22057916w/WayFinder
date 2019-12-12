package com.mapbox.mapboxandroiddemo.examples.labs.ar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.mapbox.mapboxandroiddemo.examples.labs.common.fragment.AlertFragment;
import com.mapbox.mapboxandroiddemo.examples.labs.common.fragment.ErrorFragment;
import com.mapbox.mapboxandroiddemo.examples.labs.R;
import com.mapbox.mapboxandroiddemo.examples.labs.common.fragment.NavigationFragment;
import com.mapbox.mapboxandroiddemo.examples.labs.util.ErrorManager;
import com.mapbox.mapboxandroiddemo.examples.labs.util.SearchHelper;
import com.mapbox.mapboxandroiddemo.examples.labs.util.SnackbarHelper;
import com.mapbox.mapboxandroiddemo.examples.labs.data.Destination;
import com.mapbox.mapboxandroiddemo.examples.labs.data.Poster;
import com.mapbox.mapboxandroiddemo.examples.labs.data.Vertex;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class ArImageAndNavigation extends AppCompatActivity implements Scene.OnUpdateListener{
    private static final String TAG = ArImageAndNavigation.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private ModelRenderable footRenderable;
    private ImageView fitToScanView;

    // receive data from the initiating activity (NavigationFragment or MainActivity)
    private String query = null;

    // initiating in the doMySearch() function
    private ArrayList<Vertex> vertices = new ArrayList<>();
    private Poster poster;
    private Destination dest;

    // translated information from vertices, initiating in the translation() function
    private ArrayList<Float> dist = new ArrayList<>();
    private ArrayList<Pair<Integer, Integer>> direction = new ArrayList<>();

    // assigned when invoking the createPose() method
    ArrayList<Pose> PoseList = new ArrayList<>();

    // Augmented image and its associated center pose anchor, keyed by the augmented image in
    // the database.
    private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();
    private AugmentedImage startingImage;

    // check and record the current status on OnUpdate method
    private boolean imageFound = false;
    private boolean pathFound = false;
    private boolean planeFound = false;
    private boolean destFound = false;

    // record and check if the destination and starting point are in the same floor
    private boolean sameFloor = false;
    // record the target floor for later use when "sameFloor" is true
    private String targetFloor = null;

    // check the network and threads status
    private boolean networkThreadBusy = false;

    // count turns
    private Integer count_left = 0;
    private Integer count_right = 0;

    /**
     * @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
     *  CompletableFuture requires api level 24
     *   FutureReturnValueIgnored is not valid
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        setContentView(R.layout.activity_ar_image_and_navigation);

        // get the destination from the last activity and check if it's legal
        query = getQuery();

        // assign fragment
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        fitToScanView = findViewById(R.id.image_view_fit_to_scan);

        // initiate renderable features
        loadRenderable();

        // start ar session and print route of direction
        assert arFragment != null;
        arFragment.getArSceneView().getScene().addOnUpdateListener(this);
    }

    /**
     *  get the target (room id) from either NavigationFragment or MainActivity
     *  and check wether it's legal or not if it's from MainActivity
     * @return query
     */
    private String getQuery() {
        // check if there is a network presenting
        networkChecking();
        if(Objects.equals(Objects.requireNonNull(getIntent().getExtras()).getString("id"), NavigationFragment.TAG))
            return getIntent().getExtras().getString("query");
        else {
            String query = getIntent().getExtras().getString(SearchManager.QUERY);
            // check if room name is legal
            if(SearchHelper.checkTargetName(query))
                return getIntent().getExtras().getString(SearchManager.QUERY);
            else {
                showError(ErrorManager.ROOM_NAME_ERROR);
                return null;
            }
        }
    }

    /** print AR features to the destination
     *  first, we searching the image for scanning to indicate the phone's location in the building
     *  second, once we find the image, we will fina a path from the phone's location to destination
     *  third, we check if the destination is on the different floor
     *  fourth, performing the plane finding to place the anchor of footprint as navigation to the destination
     *  finally, we calculate the distance between the phone's position and the destination to check if you get to the destination
     */
    @Override
    public void onUpdate(FrameTime frameTime) {
        //get the frame from the scene for shorthand
        Frame frame = arFragment.getArSceneView().getArFrame();

        // If there is no frame, just return.
        if (frame == null)
            return;

        // we need only one image to pin as start point, so we only do the scanning once
        if(!imageFound)
            detectingImage(frame);      // this method will alter the imageFound
        // calculating the route to the destination once we found the image(poster)
        else if(!pathFound) {
            doMySearch();               // this method will alter the pathFound
            checkFloor();               // this method will alter the sameFloor and check if they're on different floor
        }
        // once we found the route amd calculated the route, we must first find the plane and place the route anchor
        else if (!planeFound) {
            arFragment.getArSceneView().getPlaneRenderer().setEnabled(true);
            arFragment.getPlaneDiscoveryController().show();

            //get the trackables to ensure planes are detected
            for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                //If a plane has been detected & is being tracked by ARCore
                if (plane.getTrackingState() == TrackingState.TRACKING) {

                    //Hide the plane discovery helper animation and disable the place renderer
                    arFragment.getPlaneDiscoveryController().hide();
                    arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);

                    // mark the plane has been found
                    planeFound = true;

                    // create a route in the form of Pose
                    PoseList = createPose();

                    // attach pose to anchor and set footRenderable then print the route
                    ArrayList<AnchorNode> AnchorNodeList = new ArrayList<>();
                    ArrayList<Node> NodeList = new ArrayList<>();
                    assert PoseList != null;
                    for (int i = 0; i < PoseList.size(); i++) {
                        ModelRenderable modelRenderable = footRenderable;

                        AnchorNodeList.add(i, new AnchorNode(Objects.requireNonNull(arFragment.getArSceneView().getSession()).createAnchor(PoseList.get(i))));
                        AnchorNodeList.get(i).setParent(arFragment.getArSceneView().getScene());

                        NodeList.add(i, new Node());
                        NodeList.get(i).setParent(AnchorNodeList.get(i));
                        NodeList.get(i).setRenderable(modelRenderable);
                    }
                }
            }
        }
        // check if reach the destination and change the status of param "destFound"
        else if(!destFound)
            destSearch();
        else
            //noinspection UnnecessaryReturnStatement
            return;
    }

    /**
     *  This method will keep detecting the pictures until one has been detected
     *  and it will set the imageFound it one image has been found
     */
    private void detectingImage(Frame frame) {
        Collection<AugmentedImage> updatedAugmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage augmentedImage : updatedAugmentedImages) {
            // break the loop once we detected a image
            if(imageFound)
                break;
            switch (augmentedImage.getTrackingState()) {
                case PAUSED:
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    String text = "Detecting Image " + augmentedImage.getName();
                    if(!SnackbarHelper.getInstance().isShowing())
                        SnackbarHelper.getInstance().showMessage(this, text);
                    break;

                case TRACKING:
                    // Have to switch to UI Thread to update View.
                    fitToScanView.setVisibility(View.GONE);

                    // Create a new anchor for newly found images and record it.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        AugmentedImageNode node = new AugmentedImageNode(this);
                        node.setImage(augmentedImage);
                        augmentedImageMap.put(augmentedImage, node);
                        //arFragment.getArSceneView().getScene().addChild(node);

                        // record the start point, which is the image we just scan
                        startingImage = augmentedImage;
                        imageFound = true;

                        // cancel the "Detecting Image" text once totally tracking the image
                        if(SnackbarHelper.getInstance().isShowing())
                            SnackbarHelper.getInstance().hide(this);
                    }
                    break;

                case STOPPED:
                    augmentedImageMap.remove(augmentedImage);
                    break;
            }
        }
    }

    /**
     *  the function will check if the device is going online,
     *  and find the route from the query (destination) which is receive from the previous activity
     */
    private void doMySearch() {
        if(!networkThreadBusy) {
            networkThreadBusy = true;
            new Thread(() -> {
                //String result = SearchHelper.posterQuery("6", "3", query);
                String result = SearchHelper.pathQuery(startingImage, query);
                if (result.equals(ErrorManager.INTERNET_CONNECTION_ERROR) || result.equals(ErrorManager.ROOM_NAME_ERROR) || result.equals(ErrorManager.IMAGE_NAME_EROR)) {
                    showError(result);
                } else {
                    /*vertices = SearchHelper.parseCoordinateAndRotation(SearchHelper.pathQuery(startingImage, query));
                    poster = SearchHelper.parsePoster(SearchHelper.posterQuery(startingImage, query));
                    dest = SearchHelper.parseDest(SearchHelper.destQuery(query));*/

                    vertices = SearchHelper.parseCoordinateAndRotation(SearchHelper.pathQuery(startingImage, query));
                    poster = SearchHelper.parsePoster(SearchHelper.posterQuery(startingImage, query));
                    dest = SearchHelper.parseDest(SearchHelper.destQuery_test(startingImage, query));

                    // translate the vertices' information into direction and distance to calculate the route
                    translation();
                    pathFound = true;
                }
                networkThreadBusy = false;
            }).start();
        }
        Toast.makeText(this, Integer.toString(vertices.size()), Toast.LENGTH_LONG).show();
    }

    /**
     *  check if the destination is on the different floor and set the value of param "sameFloor"
     */
    private void checkFloor() {
        String currFloor = startingImage.getName().substring(2, 3);     // sf"2"33, sf"6"45
        String destFloor = query.substring(2, 3);
        sameFloor = currFloor.equals(destFloor);

        if(!sameFloor) {
            if (destFloor.equals("1"))
                targetFloor = "Ground";
            else
                targetFloor = destFloor;
        }
    }

    /**
     *  checking if the user reach the destination by the phone's local position and the destination
     *  and perform different actions, depending on the params "sameFloor"
     */
    private void destSearch() {
        Pose destPose = PoseList.get(PoseList.size() - 1);

        Vector3 currPos = arFragment.getArSceneView().getScene().getCamera().getLocalPosition();
        Vector3 destPos = new Vector3(destPose.tx(), destPose.ty() + 1.5f, destPose.tz());

        // monitoring the camera position and destination location
        String text = currPos.toString() + " || " + destPos.toString() + " || " + Vector3.subtract(currPos, destPos).length();
        Log.e("destSearch", text);

        float radius = 1.5f;
        if(Vector3.subtract(currPos, destPos).length() <= radius) {
            destFound = true;
            if(sameFloor)
                this.showDialog("You have reached the destination !!", AlertFragment.ACTIVITY_RESUME);
            else
                this.showDialog("Please take the elevator to the " + targetFloor + "-floor and re-scanning the image", AlertFragment.ACTIVITY_FINISH);
        }
    }

    /**
     * create a series of pose for routing, using self-deviced algorithm
     * to map the virtual world coordinates to the real world's.
     */
    private ArrayList<Pose> createPose() {
        ArrayList<Pose> PostList = new ArrayList<>();

        // dist and direction should have the same size
        if(dist.size() == direction.size()) {
            // initiate the starting vector
            // v0 is for right and left, v1 is for up and down, and v2 is for back and forth
            float v0 = startingImage.getCenterPose().tx();
            float v1 = startingImage.getCenterPose().ty() - 1.5f;
            float v2 = startingImage.getCenterPose().tz() + dist.get(1);
            //float v2 = startingImage.getCenterPose().tz() + greatCircle(vertices.get(0).getLat(), vertices.get(0).getLng(), poster.getLat(), poster.getLng());

            // do calibration of the coordinate system
            Pair calCoord = calibration(v0, v2);
            v0 = (float) calCoord.first;
            v2 = (float) calCoord.second;

            // the first vector is always the direction opposite to the poster
            Pair<String, String> initDirec = new Pair<>("v2", "+");

            // place the first point and determine its direction
            PostList.add(0, Pose.makeTranslation(v0, v1, v2).compose(Pose.makeRotation((float)cos(0 / 2), 0, (float)sin(0 / 2), 0)));

            // record the direction and distance
            String previous_vector = initDirec.first;
            String previous_move = initDirec.second;

            // record current direction and next direction
            Pair<Integer, Integer> currDir, nextDir;

            currDir = direction.get(0);
            for(int i = 1; i < dist.size(); i++) {
                nextDir = direction.get(i);

                // place the second point which is depend on the relative position
                // between the second vertex on the path and the poster
                if(i == 1) {
                    Pair<String, String> secondDirec = setDirection(poster, vertices.get(1), vertices.get(2));

                    assert secondDirec != null;
                    previous_vector = secondDirec.first;
                    previous_move = secondDirec.second;

                }

                switch(check(currDir, nextDir)) {
                    case "S":           // keep going forward
                        if(previous_vector.equals("v2")) {
                            if(previous_move.equals("-")) {
                                v2 -= dist.get(i);                                              // rotate about y axis for 180 degree
                                PostList.add(i, Pose.makeTranslation(v0, v1, v2).compose(Pose.makeRotation((float)cos(PI / 2), 0, (float)sin(PI / 2), 0)));
                            }
                            else {
                                v2 += dist.get(i);                                              // rotate about y axis for 0 degree
                                PostList.add(i, Pose.makeTranslation(v0, v1, v2).compose(Pose.makeRotation((float)cos(0 / 2), 0, (float)sin(0 / 2), 0)));
                            }
                        }
                        else {
                            if(previous_move.equals("-")) {
                                v0 -= dist.get(i);                                              // rotate about y axis for 90 degree
                                PostList.add(i, Pose.makeTranslation(v0, v1, v2).compose(Pose.makeRotation((float)cos((PI / 2) / 2), 0, (float)sin((PI / 2) / 2), 0)));
                            }
                            else {
                                v0 += dist.get(i);                                              // rotate about y axis for 270 degree
                                PostList.add(i, Pose.makeTranslation(v0, v1, v2).compose(Pose.makeRotation((float)cos((PI * 3 / 2) / 2), 0, (float)sin((PI * 3 / 2) / 2), 0)));
                            }
                        }
                        break;
                    case "L":           // turn left
                        count_left++;
                        if(previous_vector.equals("v2")) {
                            if(previous_move.equals("-")) {
                                v0 -= dist.get(i);                                              // rotate about y axis for 90 degree
                                PostList.add(i, Pose.makeTranslation(v0, v1, v2).compose(Pose.makeRotation((float)cos((PI / 2) / 2), 0, (float)sin((PI / 2) / 2), 0)));
                                previous_vector = "v0";
                                previous_move = "-";
                            }
                            else {
                                v0 += dist.get(i);                                              // rotate about y axis for 270 degree
                                PostList.add(i, Pose.makeTranslation(v0, v1, v2).compose(Pose.makeRotation((float)cos((PI * 3 / 2) / 2), 0, (float)sin((PI * 3 / 2) / 2), 0)));
                                previous_vector = "v0";
                                previous_move = "+";
                            }
                        }
                        else {
                            if(previous_move.equals("-")) {
                                v2 += dist.get(i);                                              // rotate about y axis for 0 degree
                                PostList.add(i, Pose.makeTranslation(v0, v1, v2).compose(Pose.makeRotation((float)cos(0 / 2), 0, (float)sin(0 / 2), 0)));
                                previous_vector = "v2";
                                previous_move = "+";
                            }
                            else {
                                v2 -= dist.get(i);                                              // rotate about y axis for 180 degree
                                PostList.add(i, Pose.makeTranslation(v0, v1, v2).compose(Pose.makeRotation((float)cos(PI / 2), 0, (float)sin(PI / 2), 0)));
                                previous_vector = "v2";
                                previous_move = "-";
                            }
                        }
                        break;
                    case "R":           // turn right
                        count_right++;
                        if(previous_vector.equals("v2")) {
                            if(previous_move.equals("-")) {
                                v0 += dist.get(i);                                          // rotate about y axis for 270 degree
                                PostList.add(i, Pose.makeTranslation(v0, v1, v2).compose(Pose.makeRotation((float)cos((PI * 3 / 2) / 2), 0, (float)sin((PI * 3 / 2) / 2), 0)));
                                previous_vector = "v0";
                                previous_move = "+";
                            }
                            else {
                                v0 -= dist.get(i);                                          // rotate about y axis for 90 degree
                                PostList.add(i, Pose.makeTranslation(v0, v1, v2).compose(Pose.makeRotation((float)cos((PI / 2) / 2), 0, (float)sin((PI / 2) / 2), 0)));
                                previous_vector = "v0";
                                previous_move = "-";
                            }
                        }
                        else {
                            if(previous_move.equals("-")) {
                                v2 -= dist.get(i);                                          // rotate about y axis for 180 degree
                                PostList.add(i, Pose.makeTranslation(v0, v1, v2).compose(Pose.makeRotation((float)cos(PI / 2), 0, (float)sin(PI / 2), 0)));
                                previous_vector = "v2";
                                previous_move = "-";
                            }
                            else {
                                v2 += dist.get(i);                                          // rotate about y axis for 0 degree
                                PostList.add(i, Pose.makeTranslation(v0, v1, v2).compose(Pose.makeRotation((float)cos(0 / 2), 0, (float)sin(0 / 2), 0)));
                                previous_vector = "v2";
                                previous_move = "+";
                            }
                        }
                        break;
                }

                currDir = nextDir;
            }

            Log.e("count_turns", "right: " + count_right + "    left: " + count_left);

            return PostList;
        }
        else {
            Log.e("creatPose", "dist size and direction are not equal");
            Log.e("creatPose", "dist size: " + dist.size());
            Log.e("creatPose", "direction size:" + direction.size());

            return null;
        }
    }

    private Pair<Float, Float> calibration(float v0, float v2) {
        float d = startingImage.getCenterPose().tz();
        float dx = startingImage.getCenterPose().tx();

        double drad = atan(dx/d);

        float x = (float) (cos(drad) * v0 - sin(drad) * v2);
        float y = (float) (sin(drad) * v0 + cos(drad) * v2);

        return new Pair<>(x, y);
    }


    private Pair<String, String> setDirection(Poster poster, Vertex vt0, Vertex vt1) {
        double dlat = vt1.getLat() - vt0.getLat();
        double dlng = vt1.getLng() - vt0.getLng();

        // check poster rotation
        String poster_direc = poster.getDirection();

        // determine the relative direction between poster and destination
        // for v2, "-" is moving forward and "+" is moving backward relative to the camera view
        // for v0, "-" is moving left and "+" is moving right relative to the camera view
        switch(poster_direc) {
            case "N":
                // check if moving south
                if(Math.abs(dlat) > Math.abs(dlng)) {
                    if(dlat < 0)
                        return new Pair<>("v2", "+");
                    else if(dlng > 0)
                        return new Pair<>("v0", "+");
                    else
                        return new Pair<>("v0", "-");
                }
                else {
                    if(dlng > 0)
                        return new Pair<>("v0", "+");
                    else
                        return new Pair<>("v0", "-");
                }
            case "S":
                // check if moving north
                if(Math.abs(dlat) > Math.abs(dlng)) {
                    if(dlat > 0)
                        return new Pair<>("v2", "+");
                    else if(dlng > 0)
                        return new Pair<>("v0", "-");
                    else
                        return new Pair<>("v0", "+");
                }
                else {
                    if(dlng > 0)
                        return new Pair<>("v0", "-");
                    else
                        return new Pair<>("v0", "+");
                }
            case "E":
                // check if moving west
                if(Math.abs(dlng) > Math.abs(dlat)) {
                    if(dlng < 0)
                        return new Pair<>("v2", "+");
                    else if(dlat > 0)
                        return new Pair<>("v0", "-");
                    else
                        return new Pair<>("v0", "+");
                }
                else {
                    if(dlat > 0)
                        return new Pair<>("v0", "-");
                    else
                        return new Pair<>("v0", "+");
                }
            case "W":
                // check if moving east
                if(Math.abs(dlng) > Math.abs(dlat)) {
                    if(dlng > 0)
                        return new Pair<>("v2", "+");
                    else if(dlat > 0)
                        return new Pair<>("v0", "+");
                    else
                        return new Pair<>("v0", "-");
                }
                else {
                    if(dlat > 0)
                        return new Pair<>("v0", "+");
                    else
                        return new Pair<>("v0", "-");
                }
        }

        return null;
    }

    /**
     * To check the next direction we need to move .
     * etc.
     * turn right, turn left, or keeping going
     */
    private String check(Pair<Integer, Integer> currDir, Pair<Integer, Integer> nextDir) {
        if(Math.abs(currDir.first) > Math.abs(currDir.second)) {            // check if the current is north-south-ward or east-west-ward
            if(currDir.first > 0) {                                        // the current direction is northward
                if(Math.abs(nextDir.first) > Math.abs(nextDir.second)) {    // check if the next direction is north-south-ward or east-west-ward
                    if(nextDir.first > 0)                                  // the next direction is northward
                        return "S";             // S for keeping the same direction
                    else                                                   // the next direction is southward
                        return "T";             // T for turning around the direction
                }
                else {                                                     // the next direction is east-west-ward
                    if(nextDir.second > 0)                                 // the next direction is eastward
                        return "R";             // R for turning right
                    else                                                   // the next direction is westward
                        return "L";             // L for turning left
                }
            }
            else {                                                         // the current direction is southward
                if(Math.abs(nextDir.first) > Math.abs(nextDir.second)) {
                    if(nextDir.first > 0)                                  // the next direction is northward
                        return "T";
                    else                                                   // the next direction is southward
                        return "S";
                }
                else {
                    if(nextDir.second > 0)                                 // the next direction is eastward
                        return "L";
                    else                                                   // the next direction is westward
                        return "R";
                }
            }
        }
        else {                                                             // the current direction direction is east-west-ward
            if(currDir.second > 0) {                                       // the current direction is eastward
                if(Math.abs(nextDir.first) > Math.abs(nextDir.second)) {
                    if(nextDir.first > 0)                                  // from east to north
                        return "L";
                    else                                                   // from east to south
                        return "R";
                }
                else {
                    if(nextDir.second > 0)
                        return "S";                                        // keep going east
                    else
                        return "T";                                        // turn around to west
                }
            }
            else {
                if(Math.abs(nextDir.first) > Math.abs(nextDir.second)) {
                    if(nextDir.first > 0)                                  // from west to north
                        return "R";
                    else                                                   // from west to south
                        return "L";
                }
                else {
                    if(nextDir.second > 0)
                        return "T";                                        // turn around to east
                    else
                        return "S";                                        // keep going west
                }
            }
        }
    }

    /**
     * transform the vertices' coordinates to a direction that based on the compass direction
     * for the later use of createPose function
     */
    private void translation() {
        // calculate distance between two adjacent points to print footprint
        for(int i = 1; i < vertices.size(); i++) {
            double lat1 = vertices.get(i - 1).getLat();
            double lng1 = vertices.get(i - 1).getLng();
            double lat2 = vertices.get(i).getLat();
            double lng2 = vertices.get(i).getLng();

            // calculate distance between two coordinates
            dist.add(greatCircle(lat1, lng1, lat2, lng2) * 0.99990853739f);

            // calculate the direction in the following lines
            double dlat = lat2 - lat1;
            double dlng = lng2 - lng1;

            if(Math.abs(dlat) > Math.abs(dlng)) {
                if(dlat > 0)
                    direction.add(new Pair<>(1, 0));     // we define (1, 0) as northward
                else
                    direction.add(new Pair<>(-1, 0));    // (-1, 0) as southward
            }
            else {
                if(dlng > 0)
                    direction.add(new Pair<>(0, 1));     // (0, 1) as eastward
                else
                    direction.add(new Pair<>(0, -1));    // (0, -1) as westward
            }
        }
    }

    /**
     *  Applying The Great Circle Theorem for calculating the distance between two coordinates
     */
    private float greatCircle(double lat1, double long1, double lat2, double long2) {
        // Convert the latitudes and longitudes
        // from degree to radians.
        lat1 = toRadians(lat1);
        long1 = toRadians(long1);
        lat2 = toRadians(lat2);
        long2 = toRadians(long2);

        // Haversine Formula
        double dlong = long2 - long1;
        double dlat = lat2 - lat1;

        double ans = pow(sin(dlat / 2), 2) +
                cos(lat1) * cos(lat2) *
                        pow(sin(dlong / 2), 2);

        ans = 2 * asin(sqrt(ans));

        // Radius of Earth in
        // Kilometers, R = 6371
        // Use R = 3956 for miles
        double R = 6371;

        // Calculate the result
        ans = ans * R;

        // km to m, double to float
        return (float)(ans * 1000);
    }

    /**When you build a Renderable, Sceneform loads its resources in the background while returning
     *  a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
     */
    private void loadRenderable() {
        ModelRenderable.builder()
                .setSource(this, Uri.parse("foot.sfb"))
                .build()
                .thenAcceptAsync(renderable -> footRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (augmentedImageMap.isEmpty()) {
            fitToScanView.setVisibility(View.VISIBLE);
        }
    }

    /**
     *  checking the network is available, if not , it may lead to crash
     */
    public void networkChecking() {
        if(!networkThreadBusy) {
            networkThreadBusy = true;
            new Thread(() -> {
                if (!SearchHelper.checkActiveInternetConnection())
                    showError("No network present");
                networkThreadBusy = false;
            }).start();
        }
    }

    /**
     *  show messages to the user
     *  such as "You have reached the destination !!"
     *  or guide the user to the different floor
     */
    private void showDialog(String message, String action) {
        DialogFragment alertFragment = AlertFragment.newInstance(message, action);
        alertFragment.show(getSupportFragmentManager(), AlertFragment.TAG);
    }

    /**
     *  show error message and finish the current activity
     */
    private void showError(String message) {
        DialogFragment errorFragement = ErrorFragment.newInstance(message, ErrorFragment.ACTIVITY_FINISH);
        errorFragement.show(getSupportFragmentManager(), ErrorFragment.TAG);
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */
    @SuppressLint("ObsoleteSdkInt")
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }
}
