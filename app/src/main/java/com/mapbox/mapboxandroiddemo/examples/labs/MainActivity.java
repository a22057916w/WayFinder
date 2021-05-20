package com.mapbox.mapboxandroiddemo.examples.labs;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxandroiddemo.examples.labs.common.fragment.NavigationFragment;
import com.mapbox.mapboxandroiddemo.examples.labs.data.FeatureData;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfJoins;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * Display an indoor map of a building with toggles to switch between floor levels
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener,  MapboxMap.OnCameraMoveListener,
        View.OnClickListener{

    private static final String GEOJSON_SOURCE_INDOOR_BUILDING_ID = "indoor-building";
    private static final String INDOOR_BUILDING_FILL_LAYER_ID = "indoor-building-fill";
    private static final String INDOOR_BUILDING_LINE_LAYER_ID = "indoor-building-line";

    // params for mapbox API dataType
    private GeoJsonSource geoSource;
    private List<List<Point>> boundingBoxList;
    private MapView mapView;
    private MapboxMap mapboxMap;

    // params for UI buttons
    private RelativeLayout level_layout;
    private HorizontalScrollView levelScrollView;
    private ImageButton buttonSerach, buttonFloor;
    private ImageButton[] levelButton = new ImageButton[9];

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_token));
        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {
            new LoadGeoJsonDataTask(MainActivity.this).execute();
            // Setting data non-relative to mapbox API and boundingbox for onCameraMove()
            setUpLocalData();

            // Setting listeners
            mapboxMap.addOnCameraMoveListener(MainActivity.this);
            mapboxMap.addOnMapClickListener(MainActivity.this);
        });

        // Declaration of UI views
        findViews();
        levelScrollView.setVisibility(View.INVISIBLE);
        // Listening on buttons except for the nine floor button
        setOnClickListener();

        // implement the nine floor button by using for loop
        // instead of implementing in setOnclickListener()
        for(int i = 0; i < levelButton.length; i++) {
            String filename = "sf_" + (i + 1) + "f.geojson";
            levelButton[i].setOnClickListener(v -> geoSource.setGeoJson(loadGeoJsonFromAsset(filename)));
        }
    }

    public void findViews() {
        level_layout = findViewById(R.id.level_layout);
        levelScrollView = findViewById(R.id.level_scroll_view);
        buttonSerach = findViewById(R.id.search_room_buttons);
        buttonFloor = findViewById((R.id.floor_level_buttons));

        // declaration of buttons of nine floors
        String[] floorButtonId = {"ground", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth"};
        for(int i = 0; i < levelButton.length; i++) {
            String buttonID = floorButtonId[i] + "_floor_button";
            int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
            levelButton[i] = findViewById(resID);
        }
    }

    /**
     * The underlying methods handle the actions of button and clicks
     * etc. setOnClickListener(), onClick, OnCameraMove(), hideLevelButton(), showLevelButton()
     */
    private void setOnClickListener() {
        buttonSerach.setOnClickListener(this);
        buttonFloor.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.search_room_buttons:
                onSearchRequested();
                break;
            case R.id.floor_level_buttons:
                // Setting layout appearance
                if(level_layout.getVisibility() == View.INVISIBLE)
                    level_layout.setVisibility(View.VISIBLE);
                else
                    level_layout.setVisibility(View.INVISIBLE);

                // Setting ScrollView appearance
                if(levelScrollView.getVisibility() == View.INVISIBLE)
                    levelScrollView.setVisibility(View.VISIBLE);
                else
                    levelScrollView.setVisibility(View.INVISIBLE);
                break;
        }
    }


    @Override
    public void onCameraMove() {
        if (mapboxMap.getCameraPosition().zoom > 16) {
            if (TurfJoins.inside(Point.fromLngLat(mapboxMap.getCameraPosition().target.getLongitude(),
                    mapboxMap.getCameraPosition().target.getLatitude()), Polygon.fromLngLats(boundingBoxList))) {
                if (buttonFloor.getVisibility() != View.VISIBLE) {
                    showLevelButton();
                }
            } else {
                if (buttonFloor.getVisibility() == View.VISIBLE) {
                    hideLevelButton();
                }
            }
        } else if (buttonFloor.getVisibility() == View.VISIBLE) {
            hideLevelButton();
        }
    }

    /**
     *  When the user moves away from our bounding box region or zooms out far enough the floor level
     *  buttons are faded out and hidden.
     */
    private void hideLevelButton() {
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(500);
        buttonFloor.startAnimation(animation);
        buttonFloor.setVisibility(View.GONE);
    }

    /**
     *  When the user moves inside our bounding box region or zooms in to a high enough zoom level,
     *  the floor level buttons are faded out and hidden.
     */
    private void showLevelButton() {
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(500);
        buttonFloor.startAnimation(animation);
        buttonFloor.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return handleClickIcon(mapboxMap.getProjection().toScreenLocation(point));
        //return true;
    }

    /**
     *  initialing this method when user press a room on the building map
     *  and engage the navigationFragment
     * @param screenPoint
     */
    private boolean handleClickIcon(PointF screenPoint) {
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, INDOOR_BUILDING_FILL_LAYER_ID);
        if (!features.isEmpty()) {

            // display the self-custom navigation dialog
            DialogFragment navigationFragment = new NavigationFragment();
            Bundle args = new Bundle();
            args.putSerializable("featureData", new FeatureData(features.get(0)));
            navigationFragment.setArguments(args);
            navigationFragment.show(getSupportFragmentManager(), NavigationFragment.TAG);

            return true;
        } else {
            return false;
        }
    }

    /**
     * handling search request for room name, and jump to SearchActivity
     * using search dialog, declaration is in AndroidManifest.xml
     */
    @Override
    public boolean onSearchRequested() {
        Bundle appSearchData = new Bundle();
        appSearchData.putString("KEY", "text");

        startSearch(null, false, appSearchData, false);
        return true;
    }

    /**
     * setting up initial map data and update data for button clicking for level changing and reload the map
     * etc. setUpLocalData(), setUpMapData(), setUpSource(), setUpBuildingLayer()
     */
    public void setUpLocalData() {

        // Setting SF building boundary points for method onCameraMove() to check the position of the camera
        final List<Point> boundingBox = new ArrayList<>();
        boundingBox.add(Point.fromLngLat(121.43205264316691, 25.035995935944868));
        boundingBox.add(Point.fromLngLat(121.43138715315331, 25.035961812589107));
        boundingBox.add(Point.fromLngLat(121.43145521507057, 25.034981252735363));
        boundingBox.add(Point.fromLngLat(121.43210783411593, 25.035015204796736));

        boundingBoxList = new ArrayList<>();
        boundingBoxList.add(boundingBox);
    }

    public void setUpMapData() {
        if (mapboxMap != null) {
            mapboxMap.getStyle(style -> {
                setupSource(style);
                setUpBuildingLayer(style);
            });
        }
    }

    /**
     *  Method used to load the geoSource on the map for the very first time
     * @param style
     */
    private void setupSource(@NonNull Style style) {
        geoSource = new GeoJsonSource(
                GEOJSON_SOURCE_INDOOR_BUILDING_ID, loadGeoJsonFromAsset("sf_6f.geojson"));
        style.addSource(geoSource);
    }

    /**
     *  Method used to load the indoor layer on the map. First the fill layer is drawn and then the
     *  line layer is added.
     * @param style
     */
    private void setUpBuildingLayer(@NonNull Style style) {


        FillLayer indoorBuildingLayer = new FillLayer(INDOOR_BUILDING_FILL_LAYER_ID, GEOJSON_SOURCE_INDOOR_BUILDING_ID).withProperties(
                fillColor(Color.parseColor("#eeeeee")),
                // Function.zoom is used here to fade out the indoor layer if zoom level is beyond 16. Only
                // necessary to show the indoor map at high zoom levels.
                fillOpacity(interpolate(exponential(1f), zoom(),
                        stop(16f, 0f),
                        stop(16.5f, 0.5f),
                        stop(17f, 1f))));

        style.addLayer(indoorBuildingLayer);

        LineLayer indoorBuildingLineLayer = new LineLayer(INDOOR_BUILDING_LINE_LAYER_ID, GEOJSON_SOURCE_INDOOR_BUILDING_ID).withProperties(
                lineColor(Color.parseColor("#50667f")),
                lineWidth(0.5f),
                lineOpacity(interpolate(exponential(1f), zoom(),
                        stop(16f, 0f),
                        stop(16.5f, 0.5f),
                        stop(17f, 1f))));
        style.addLayer(indoorBuildingLineLayer);
    }

    /**
     *  open another thread to load the GeoJSON data
     */
    private static class LoadGeoJsonDataTask extends AsyncTask<Void, Void, FeatureCollection> {

        private final WeakReference<MainActivity> activityRef;

        LoadGeoJsonDataTask(MainActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected FeatureCollection doInBackground(Void... params) {
            MainActivity activity = activityRef.get();

            if (activity == null) {
                return null;
            }

            String geoJson = activity.loadGeoJsonFromAsset("sf_6f.geojson");
            return FeatureCollection.fromJson(Objects.requireNonNull(geoJson));
        }

        @Override
        protected void onPostExecute(FeatureCollection featureCollection) {
            super.onPostExecute(featureCollection);
            MainActivity activity = activityRef.get();
            if (featureCollection == null || activity == null) {
                return;
            }
            activity.setUpMapData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * Using this method to load in GeoJSON files from the assets folder.
     */
    private String loadGeoJsonFromAsset(String filename) {
        String dir = "blueprint/";
        try {
            InputStream is = getAssets().open(dir + filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, StandardCharsets.UTF_8);

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

}