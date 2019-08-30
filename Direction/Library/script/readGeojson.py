import json

def readAllGeojson():
    geoSource = [None] * 9
    for i in range(0, len(geoSource)):
        fileName = "C:/Users/w/Documents/GitHub/WayFinder/Direction/Library/geojson/sf_" + str(i + 1) + "f.geojson"
        with open(fileName, "r", encoding="UTF-8") as reader:
            geoSource[i] = json.loads(reader.read())
    return geoSource
