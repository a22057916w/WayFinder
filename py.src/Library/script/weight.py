import json
import re
from math import sin, cos, sqrt, atan2, radians
import sys
# Adding the path of self-def Library
sys.path.append("C:/Users/A02wxy/Documents/GitHub/WayFinder/Direction/Library/script/")
from featureCollection import Feature, Vertex
from readGeojson import readAllGeojson


def getWeight(floorVertex):
    floorNumber = len(floorVertex)

    floors_weight = []
    for i in range(0, floorNumber):
        weight = calWeight(floorVertex[i])        # return weights of one floor w[][]
        floors_weight.append(weight)
    return floors_weight


def calWeight(oneFloorVertex):
    vertex = oneFloorVertex

    n = len(vertex)     # count the number of vertexes
    weight = [[1E9 for i in range(n)] for j in range(n)]
    for i in range(0, n):
        weight[i][i] = 0

    for i in range(0, n):
        spID = vertex[i].getID()                    # If id = "c13"
        spAB = re.split("[0-9]+", spID)[0]          # return 'c'
        spNum = int(re.split("[a-z]+", spID, flags = re.IGNORECASE)[1])    # return int("13")
        for j in range(0, n):
            epID = vertex[j].getID()
            epAB = re.split("[0-9]+", epID)[0]
            epNum = int(re.split("[a-z]+", epID, flags = re.IGNORECASE)[1])
            # If the value equals to 0, the point is on the same hallway
            if ord(spAB) - ord(epAB) == 0:
                # If the value equals to 1, there is at least a point nearby
                if abs(spNum - epNum) == 1:
                    weight[i][j] = The_Great_Circle_Theorem(vertex[i].coordinates, vertex[j].coordinates)
                    weight[i][j] *= 1000      # Converting unit km to m

    # Checking if points overlap each other (Checking if there is an intersection)
    for i in range(0, n):
        if vertex[i].isInct():
            inct_id = vertex[i].getInct()
            inct_index = 0
            for j in range(0, n):
                if inct_id == vertex[j].getID():
                    inct_index = vertex[j].getIndex()
                    break
            #print(vertex[i].getID(), vertex[inct_index].getID())
            for j in range(0, n):
                if weight[inct_index][j] != 1E9:
                    weight[i][j] = weight[inct_index][j]
    return weight

# Applying The Great Circle Theorem for calculating the distance between two coordinates
def The_Great_Circle_Theorem(coordinates1, coordinates2):
    lng1, lat1 = radians(coordinates1[0]), radians(coordinates1[1])
    lng2, lat2 = radians(coordinates2[0]), radians(coordinates2[1])

    R = 6373.0 # radius of Earth

    # The Great Circle Theorem formula
    dlon = lng2 - lng1
    dlat = lat2 - lat1
    a = (sin(dlat/2))**2 + cos(lat1) * cos(lat2) * (sin(dlon/2))**2
    c = 2 * atan2(sqrt(a), sqrt(1-a))
    distance = R * c

    return distance
