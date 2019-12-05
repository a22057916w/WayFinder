import mysql.connector
import pymysql
from sqlalchemy import create_engine

def MY_ENGINE():

    # ====== Connection ====== #
    # Connecting to mysql by providing a sqlachemy engin
    engine = create_engine('mysql+mysqlconnector://root:@140.136.150.100:3306/v3_WayFinder', echo=False)
    return engine
