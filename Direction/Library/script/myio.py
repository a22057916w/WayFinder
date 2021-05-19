import pandas as pd
import json
import os

def read_excel(fname):
    try:
        df = pd.read_excel(fname)
        df = df.fillna("NULL")
        #my_dict = df.to_dict("records")
        #return my_dict  # return a list of dict
        return df

    except Exception as e:
        print(e)

def save(data, fname):

    df = pd.DataFrame.from_dict(data)
    writer = pd.ExcelWriter(fname + ".xlsx", engine='xlsxwriter')
    df.to_excel(writer)
    writer.save()

    with open(fname + ".json", 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, sort_keys=True, ensure_ascii=False)
