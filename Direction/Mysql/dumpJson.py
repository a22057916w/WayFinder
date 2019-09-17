import mysql.connector
import json
import re
from math import sin, cos, sqrt, atan2, radians
import sys
# Adding the path of self-def Library
sys.path.append("C:/Users/w/Documents/GitHub/WayFinder/Direction/Library/script/")
from featureCollection import Feature, Vertex
from readGeojson import readAllGeojson

def MYSQL_DUMP_JSON():
    mydb = mysql.connector.connect(
      host="140.136.150.100",
      user="root",
      database="WayFinder"
    )
    mycursor = mydb.cursor()

    # read all geoJson data
    geoSource = readAllGeojson()

    for i in range(0, len(geoSource)):
        floor = "sf_" + str(i + 1) + "f"

        # initiating features from geoJson Data
        floorData = geoSource[i]
        floorFeatures = floorData["features"]
        features = []
        for feature in floorFeatures:
            Afeature = Feature(feature)
            features.append(Afeature)

        # checking if table exists
        sql_checkTable = "SHOW TABLES LIKE %s"
        mycursor.execute(sql_checkTable, (floor, ))
        result = mycursor.fetchone()
        if result == None:
            sql_create = "CREATE TABLE " + floor + "(type VARCHAR(255), id VARCHAR(255), name VARCHAR(255), multi_door int(255), door VARCHAR(255), vertex VARCHAR(255), vertex_id VARCHAR(255))"
            mycursor.execute(sql_create)
        else:
            sql_delete = "DELETE FROM " + floor
            mycursor.execute(sql_delete)

        # Updating mysql data from features
        sql = "INSERT INTO " + floor + "(type, id, name, multi_door, door, vertex, vertex_id) VALUES (%s, %s, %s, %s, %s, %s, %s)"
        for feature in features:
            type = feature.getType()
            id = feature.getID()
            name = feature.getName()
            mutil_door = feature.getMutilDoors()

            door = str(feature.getDoor())
            vertex = str(feature.getVertex())
            vertex_id = str(feature.getVertexID())

            val = (type, id, name, mutil_door, door, vertex, vertex_id)
            mycursor.execute(sql, val)

    mydb.commit()
    mydb.close()

MYSQL_DUMP_JSON()
