import mapbox
import json
from math import sin, cos, sqrt, atan2, radians
import sys
# Adding the path of self-def Library
sys.path.append("C:/Users/w/Documents/GitHub/WayFinder/Direction/Library/script/")
from featureCollection import Feature, Vertex
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
    #print(d)
    return d, next


# 印出由s點到t點的最短路徑
def find_path(s, t, next):
    i = s
    while i != t:
        i = next[i][t]
        print(i)
    print(t)


def getRoute(collection, targetCollection):
    # assigning the variables
    dist = collection["dist"]
    targetDist = targetCollection["dist"]

    sn = len(dist[0])
    tn = len(targetDist[0])
    n = max(sn, tn)
    route = [[1E9 for i in range(n)] for j in range(n)]
    for i in range(0, sn):
        for j in range(0, tn):
            route[i][j] = getDistance(collection, targetCollection, i, j)
    return route

def getDistance(collection, targetCollection, s, e):
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
    for elevator in elevators:
        VID = elevator.getVertexID()[0]
        spe = dist[s][EVID[VID]]
        for targetElevator in targetElevators:
            TVID = targetElevator.getVertexID()[0]
            tpe = targetDist[e][TEVID[TVID]]
            totDist = min(totDist, spe + tpe)

    return totDist

def parseSave(totDist, floorVertex, floorNext):
    floorNumber = len(totDist)  # len(totDist) gets floor counts

    # parse and save each floor as a file for dist and next
    for i in range(0, floorNumber):
        fileDist = "C:/Users/w/Documents/GitHub/WayFinder/Direction/Route/dist/sf" + str(i + 1) + "f_dist"
        dist = []       # for saving dist to each floor from floor[i]
        # parsing the dist data
        for j in range(0, floorNumber):
            for k in range(0, len(totDist[i][j])):
                for l in range(0, len(totDist[i][j][k])):
                    dist.append({
                        "floor": str(j),
                        "start": str(k),
                        "dest": str(l),
                        "dist": totDist[i][j][k][l]
                    })
        save(dist, fileDist)    # saving using myio function

        # parsing the next(middle points) data and save
        for i in range(0, floorNumber):
            fileNext = "C:/Users/w/Documents/GitHub/WayFinder/Direction/Route/next/sf" + str(i + 1) + "f_next"
            middle = []     # for saving middle points between the route only on floor[i]
            for j in range(0, len(floorNext[i])):
                for k in range(0, len(floorNext[i][j])):
                    middle.append({
                        "start": str(j),
                        "dest": str(k),
                        "next": floorNext[i][j][k]
                    })
            save(middle, fileNext)  # saving using myio function


    #store each floor's vertexes
    for i in range(0, floorNumber):
        fileName = "C:/Users/w/Documents/GitHub/WayFinder/Direction/Route/vertex/sf" + str(i + 1) + "f_vertex"
        vertexes = []
        for vertex in floorVertex[i]:
            vertexes.append({
                "ID": vertex.getID(),
                "index": vertex.getIndex(),
                "inct": vertex.getInct(),
                "coordinate": vertex.getCoordinate()
            })
        save(vertexes, fileName)

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


    # calculating each floor's route
    totalWeight = getWeight(floorVertex)       # return a list of weight (index represent floor)
    floorDist = []
    floorNext = []
    for i in range(0, len(totalWeight)):
        d, n = Floyd_Warshall(totalWeight[i])       # return two list of two dim
        floorDist.append(d)
        floorNext.append(n)

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

    # calculating the route for all floor
    totalRoute = []     # a four dim list [startFloor][targetFloor][startPoint][endPoint]
    for i in range(0, floorNumber):
        route = []
        for j in range(0, floorNumber):
            if i == j:
                route.append(floorDist[i])
                continue
            route.append(getRoute(floorCollection[i], floorCollection[j]))
        totalRoute.append(route)

    parseSave(totalRoute, floorVertex, floorNext)
