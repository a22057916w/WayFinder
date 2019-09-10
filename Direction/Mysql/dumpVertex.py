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

def MYSQL_DUMP_DIST():

    # ====== Connection ====== #
    # Connecting to mysql by providing a sqlachemy engine
    engine = create_engine('mysql+mysqlconnector://root:@140.136.150.100:3306/WayFinder', echo=False)

    # dump dist excel to mysql
    floorNumber = 9
    for i in range(0, floorNumber):
        fileName = "C:\\Users\\w\\Documents\\GitHub\\WayFinder\\Direction\\Route\\dist\\sf" + str(i + 1) + "f_vertex.xlsx"
        tableName = "sf" + str(i + 1) + "f_vertex"
        df = read_excel(fileName)
        df.to_sql(name = tableName, if_exists="replace", con = engine, index = False)

MYSQL_DUMP_DIST()
