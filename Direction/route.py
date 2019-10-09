import mapbox
import json
from math import sin, cos, sqrt, atan2, radians
import sys
# Adding the path of self-def Library
sys.path.append("C:/Users/A02wxy/Documents/GitHub/WayFinder/Direction/Library/script/")
from featureCollection import Feature, Vertex, Poster
from readGeojson import readAllGeojson
from weight import getWeight
from myio import save

# all pairs shortest path alogrithm
def Floyd_Warshall(weight):
    w = weight                  # 一張有權重的圖
    n = len(w[0])          # w[n][n]
    d = [[0 for i in range(n)] for j in range(n)]       # 最短路徑長度
    next = [[0 for i in range(n)] for j in range(n)]    # 由i點到j點的路徑，第二點為next[i][j]

    for i in range(0, n):
        for j in range(0, n):
            d[i][j] = w[i][j]
            next[i][j] = j

    for i in range(0, n):
        d[i][i] = 0

    for k in range(0, n):     # 嘗試每一個中繼點
        for i in range(0, n): # 計算每一個i點與每一個j點
            for j in range(0, n):
                if d[i][k] + d[k][j] < d[i][j]:
                    d[i][j] = d[i][k] + d[k][j]

                    # 由i點到j點的路徑的第二點，
                    # 正是由i點到k點的路徑的第二點。
                    next[i][j] = next[i][k];

    totNext = [[None for i in range(n)] for j in range(n)]
    for i in range(0, n):
        for j in range(0, n):
            totNext[i][j] = find_path(i, j, next)

    return d, next, totNext


# 印出由s點到t點的最短路徑
def find_path(s, t, next):
    middle = []
    i = s
    while i != t:
        i = next[i][t]
        middle.append(i)
    return middle


def getRoute(collection, targetCollection):
    # assigning the variables
    dist = collection["dist"]
    targetDist = targetCollection["dist"]

    sn = len(dist[0])
    tn = len(targetDist[0])
    n = max(sn, tn)
    route = [[1E9 for i in range(n)] for j in range(n)]
    EID = [[None for i in range(n)] for j in range(n)]      # record which elevator it takes
    for i in range(0, sn):
        for j in range(0, tn):
            route[i][j], EID[i][j] = getDistanceAndElevator(collection, targetCollection, i, j)
    return route, EID

def getDistanceAndElevator(collection, targetCollection, s, e):
    # assigning the variables
    dist = collection["dist"]
    elevators = collection["elevators"]
    vertexes = collection["vertexes"]

    targetDist = targetCollection["dist"]
    targetElevators = targetCollection["elevators"]
    targetVertex = targetCollection["vertexes"]

    # create a hashmap<string, integer>(<VID, index>)
    EVID = {}
    for elevator in elevators:
        VID = elevator.getVertexID()[0]
        for vertex in vertexes:
            if vertex.getID() == VID:
                EVID[VID] = vertex.getIndex()
                break
    TEVID = {}
    for elevator in targetElevators:
        TVID = elevator.getVertexID()[0]
        for vertex in targetVertex:
            if vertex.getID() == TVID:
                TEVID[TVID] = vertex.getIndex()
                break

    # calculating the route across floors
    spe = 1E9     # starting point to elevator
    tpe = 1E9     # target point to elevator
    totDist = 1E9
    EID = None    # record the elevator's id
    for elevator in elevators:
        VID = elevator.getVertexID()[0]
        spe = dist[s][EVID[VID]]
        EID = elevator.getID()
        # find the same elevator on different floor
        for targetElevator in targetElevators:
            if targetElevator.getName() == elevator.getName():
                TVID = targetElevator.getVertexID()[0]
                tpe = targetDist[e][TEVID[TVID]]
                if totDist > spe + tpe:
                    totDist = spe + tpe
                    EID = targetElevator.getID()
                break
            else:
                continue

    return totDist, EID

def parseSave(totDist, totElev, floorVertex, floorNext, floorRouteCoord, floorRouteRot):
    floorNumber = len(totDist)  # len(totDist) gets floor counts

    """# parse and save each floor as a file for dist and next
    for i in range(0, floorNumber):
        fileDist = "C:/Users/A02wxy/Documents/GitHub/WayFinder/Direction/Route/dist/sf" + str(i + 1) + "f_dist"
        dist = []       # for saving dist to each floor from floor[i]
        # parsing the dist data
        for j in range(0, floorNumber):
            for k in range(0, len(totDist[i][j])):
                for l in range(0, len(totDist[i][j][k])):
                    dist.append({
                        "floor": str(j),
                        "start": str(k),
                        "dest": str(l),
                        "dist": totDist[i][j][k][l],
                        "Elevator": totElev[i][j][k][l]
                    })
        save(dist, fileDist)    # saving using myio function

    # parsing the next(middle points) data and save
    for i in range(0, floorNumber):
        fileNext = "C:/Users/A02wxy/Documents/GitHub/WayFinder/Direction/Route/next/sf" + str(i + 1) + "f_next"
        middle = []     # for saving middle points between the route only on floor[i]
        for j in range(0, len(floorNext[i])):
            for k in range(0, len(floorNext[i][j])):
                middle.append({
                    "start": str(j),
                    "dest": str(k),
                    "next": floorNext[i][j][k],
                    "coordinate": floorRouteCoord[i][j][k],
                    "rotation": floorRouteRot[i][j][k]
                })
        save(middle, fileNext)  # saving using myio function


    #store each floor's vertexes
    for i in range(0, floorNumber):
        fileName = "C:/Users/A02wxy/Documents/GitHub/WayFinder/Direction/Route/vertex/sf" + str(i + 1) + "f_vertex"
        vertexes = []
        for vertex in floorVertex[i]:
            vertexes.append({
                "ID": vertex.getID(),
                "_index_": vertex.getIndex(),
                "inct": vertex.getInct(),
                "coordinate": vertex.getCoordinate(),
                "rotation": vertex.getRotation()
            })
        save(vertexes, fileName)"""

    # store each floor's poster
    for i in range(0, floorNumber):
        fileName = "C:/Users/A02wxy/Documents/GitHub/WayFinder/Direction/Route/poster/sf" + str(i + 1) + "f_poster"
        posters = []
        for poster in floorPoster[i]:
            posters.append({
                "ID": poster.getID(),
                "coordinate": poster.getCoordinate(),
                "vertex_id": poster.getVertexID()
            })
        save(posters, fileName)

if __name__ == "__main__":
    geoSource = readAllGeojson()
    floorNumber = len(geoSource)
    # initializing each floor's data
    floorData = []
    for i in range(0, floorNumber):
        floorData.append(geoSource[i])

    # initializing each floor's feature with two dim list
    floorFeatures = []
    for i in range(0, floorNumber):
        features = []
        for feature in floorData[i]["features"]:
            features.append(Feature(feature))
        floorFeatures.append(features)

    # initializing each floor's vertexes
    floorVertex = []
    for i in range(0, floorNumber):
        index = 0
        vertex = []
        for feature in floorFeatures[i]:
            if feature.getType() == "point":
                vertex.append(Vertex(feature.getFeature(), index))
                index += 1
        floorVertex.append(vertex)

    # initializing each floor's elevators
    floorElevators = []
    for i in range(0, floorNumber):
        elevator = []
        for feature in floorFeatures[i]:
            if feature.getType() == "elevator":
                elevator.append(Feature(feature.getFeature()))
        floorElevators.append(elevator)

    # initializing each floor's poster
    floorPoster = []
    for i in range(0, floorNumber):
        poster = []
        for feature in floorFeatures[i]:
            if feature.getType() == "poster":
                poster.append(Poster(feature.getFeature()))
        floorPoster.append(poster)

    # calculating each floor's route
    totalWeight = getWeight(floorVertex)       # return a list of weight (index represent floor)
    floorDist = []
    floorNext = []
    totalNext = []
    for i in range(0, len(totalWeight)):
        d, n , tn = Floyd_Warshall(totalWeight[i])       # return two list of two dim
        floorDist.append(d)
        floorNext.append(n)
        totalNext.append(tn)

    # store all the data of each floor
    floorCollection = []
    for i in range(0, floorNumber):
        floorCollection.append({
            "features": floorFeatures[i],
            "vertexes": floorVertex[i],
            "elevators": floorElevators[i],
            "dist": floorDist[i],
            "next": floorNext[i]
        })

    # calculating the route and other information for all floor
    totalRoute = []     # a four dim list [startFloor][targetFloor][startPoint][endPoint]
    totalElevator = []  # a four dim list to record the elevator between two points
    for i in range(0, floorNumber):
        routes = []
        EIDS = []
        for j in range(0, floorNumber):
            if i == j:
                routes.append(floorDist[i])

                # n for numbers of vertex of each floor
                n = len(floorVertex[i])
                EIDS.append([[None for i in range(n)] for j in range(n)])
                continue

            route, EID = getRoute(floorCollection[i], floorCollection[j])
            routes.append(route)
            EIDS.append(EID)

        totalRoute.append(routes)
        totalElevator.append(EIDS)

    # change a list of index number into  a list of coordinate
    totalRouteCoord = []
    for i in range(0, floorNumber):
        floorRouteCoord = []
        for j in range(0, len(totalNext[i])):
            routeCoord = []
            for k in range(0, len(totalNext[i][j])):
                coords = []
                for vertex_index in totalNext[i][j][k]:
                    coords.append(floorVertex[i][vertex_index].getCoordinate())
                routeCoord.append(coords)
            floorRouteCoord.append(routeCoord)
        totalRouteCoord.append(floorRouteCoord)

    totalRouteRot = []
    for i in range(0, floorNumber):
        floorRouteRot = []
        for j in range(0, len(totalNext[i])):
            routeRot = []
            for k in range(0, len(totalNext[i][j])):
                rotation = []
                for vertex_index in totalNext[i][j][k]:
                    rotation.append(floorVertex[i][vertex_index].getRotation())
                routeRot.append(rotation)
            floorRouteRot.append(routeRot)
        totalRouteRot.append(floorRouteRot)

    parseSave(totalRoute, totalElevator, floorVertex, totalNext, totalRouteCoord, totalRouteRot)
