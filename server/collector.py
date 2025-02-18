from flask import Flask, jsonify, request
import firebase_admin
from firebase_admin import credentials, db
from sklearn.neighbors import KNeighborsClassifier  # Import a classifier
from sklearn.model_selection import train_test_split
from collections import deque
import pandas as pd
import numpy as np

SWIPE_COLUMNS = [
    "StartX", "StartY", "EndX", "EndY", 
    "Length", "Duration", "BoundsArea", "MidpointPressure",
    "Q1Velocity", "Q2Velocity", "Q3Velocity", "Q4Velocity", 
    "Q1Pressure", "Q2Pressure", "Q3Pressure", "Q4Pressure"
]

user_table = {}

def fetch_data(table: str):
    setup_firebase()
    ref = db.reference(table)
    data = ref.get()
    return data

def setup_firebase():
    cred_obj = credentials.Certificate('secret.json')
    url = 'https://touchalytics-example-default-rtdb.firebaseio.com/'

    if not firebase_admin._apps:
        firebase_admin.initialize_app(cred_obj, {'databaseURL': url})

def collect_swipes():
    global user_table
    swipe_data = fetch_data("/Swipes/")
    swipes = pd.DataFrame.from_dict(swipe_data).transpose()
    # print(swipes)

    user_table = {}
    swipes_X = []
    swipes_y = []

    for index, swipe in swipes.iterrows():
        if f"{int(swipe["UserID"])}" not in user_table.keys():
            user_table[f"{int(swipe["UserID"])}"] = len(user_table.keys())
        x = []
        for col in SWIPE_COLUMNS:
            x.append(swipe[col])
        swipes_X.append(x)
        swipes_y.append(user_table[f"{int(swipe["UserID"])}"])

    print(f"Collected {len(swipes_y)} examples")

    return swipes_X, swipes_y
        
def train():
    X, y = collect_swipes()
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)
    
    model = KNeighborsClassifier(n_neighbors=5)
    model.fit(np.array(X_train), np.array(y_train))

    acc = model.score(X_test, y_test)
    print(f"Accuracy={acc*100}%")

def main():
    train()
    
if __name__ == "__main__":
    main()
