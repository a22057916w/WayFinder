import mysql.connector
import json
import re
import pandas as pd
import pymysql
from sqlalchemy import create_engine
import sys
# Adding the path of self-def Library
sys.path.append("C:/Users/w/Documents/GitHub/WayFinder/Direction/Library/script/")
from featureCollection import Feature, Vertex
from myio import read_excel

def MYSQL_DUMP_VERTEX():

    # ====== Connection ====== #
    # Connecting to mysql by providing a sqlachemy engine
    engine = MY_ENGINE()

    # dump dist excel to mysql
    floorNumber = 9
    for i in range(0, floorNumber):
        fileName = "C:\\Users\\w\\Documents\\GitHub\\WayFinder\\Direction\\Route\\vertex\\sf" + str(i + 1) + "f_vertex.xlsx"
        tableName = "sf" + str(i + 1) + "f_vertex"
        df = read_excel(fileName)
        df.to_sql(name = tableName, if_exists="replace", con = engine, index = False)

MYSQL_DUMP_VERTEX()
