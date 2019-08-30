import sys
import json
import re

class Feature():
    # This scope belongs to Global (static variable in C/C++ or JAVA)
    # If you declare a variable such as a LIST, you will get an unexpected error
    def __init__(self, feature):
        # This scope belongs to local variable for an individual instance

        # Data initializing
        self.geometry = {}
        self.properties = {}
        # Data belongs to property
        self.type = None                 # String
        self.id = None                   # String
        self.name = None                 # String
        self.multi_doors = False         # Booleam

        self.door = []                   # List of coordinates (List of Tuple)
        self.vertex = []                 # List of the closest point on the corridor to the feature's door (List of Tuple)
        self.vertex_id = []              # list of String

        self.feature = None

        # Data assigning
        self.feature = feature
        self.geometry = feature["geometry"]
        self.properties = feature["properties"]
        self.setData()

    def setData(self):
        self.type = self.properties["type"]                 # Type of String
        self.id = self.properties["id"]                     # Type of String

        # There is no name for a point
        if self.type != "point":
            self.name = self.properties["name"]              # Type of String

        # Checking if the Type of the feature is not a cooridor or a point,
        # if not, initialing the remaining variable
        if self.type != "corridor" and self.type != "point" and self.type != "hall":
            self.multi_doors = bool(int(self.properties["multi_door"]))   # Type of Boolean


            # Checking if there is mutiple door in a room
            if self.multi_doors:
                coordinates = self.properties["door"].split(";")
                vertexes = self.properties["vertex"].split(";")
                vertexes_id = self.properties["vertex_id"].split(";")

                # initialing door
                for coordinate in coordinates:
                    latlng = coordinate.split(",")
                    self.door.append((latlng[0], latlng[1]))    # longitude, latitude

                # initialing vertex
                for ver in vertexes:
                    latlng = ver.split(",")
                    self.vertex.append((latlng[0], latlng[1]))

                # initialing vertex_id
                for v_id in vertexes_id:
                    self.vertex_id.append(v_id)
            else:
                latlng = self.properties["door"].split(",")
                self.door.append((latlng[0], latlng[1]))        # longitude, latitude

                latlng = self.properties["vertex"].split(",")
                self.vertex.append((latlng[0], latlng[1]))

                self.vertex_id.append(self.properties["vertex_id"])

    def getType(self):
        return self.type
    def getID(self):
        return self.id
    def getName(self):
        return self.name
    def getMutilDoors(self):
        return self.multi_doors
    def getDoor(self):
        return self.door
    def getVertex(self):
        return self.vertex
    def getVertexID(self):
        return self.vertex_id
    def getFeature(self):
        return self.feature

    def printDetails(self):
        print("Type:", self.type)
        print("Name:", self.name)
        print("ID:", self.id)

        if self.multi_doors:
            print("This room has mutiple doors:", self.door)
            print("The closest vertex to each door", self.vertex)
        else:
            print("This room has only one door:", self.door)
            print("The closest vertex to the door", self.vertex)
        return None

class Vertex():
    def __init__(self, feature, index):
        # Data initializing
        self.geometry = {}
        self.properties = {}
        self.type = None
        self.id = None                        # Type of String
        self.index = 0                  # Type of Integer

        self.onInct = False             # Type of Boolean
        self.inct = None                # Type of String

        self.coordinates = None         # Type of list of String
        self.lat = 0                    # Type of Double
        self.lng = 0                    # Type of Double

        self.feature = None

        # Data assigning
        self.index = index
        self.feature = feature
        self.geometry = feature["geometry"]
        self.properties = feature["properties"]
        self.setData()

    def setData(self):
        # Setting geometry keys
        self.coordinates = self.geometry["coordinates"]
        self.lng = float(self.coordinates[0])
        self.lat = float(self.coordinates[1])

        # Setting properties keys
        self.type = self.properties["type"]
        self.id = self.properties["id"]

        # Checking if the point intersect with others
        if self.properties.__contains__("inct"):
            self.inct = self.properties["inct"]
            self.onInct = True

    def getIndex(self):
        return self.index
    def getType(self):
        return self.type
    def getID(self):
        return self.id
    def getCoordinate(self):
        return self.coordinates
    def getLat(self):
        return self.lat
    def getLng(self):
        return self.lng
    def getFeature(self):
        return self.feature
    def getInct(self):
        if self.isInct():
            return self.inct
    def isInct(self):
        return self.onInct
