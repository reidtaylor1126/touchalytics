from flask import Flask, jsonify, request
import firebase_admin
from firebase_admin import credentials, db
from firebase_utils import *
from sklearn.neighbors import KNeighborsClassifier  # Import a classifier
from sklearn.model_selection import train_test_split
from collections import deque
import pandas as pd
import numpy as np

MAX_INVALID_SWIPES = 8
LEARN_THRESHOLD = 50

SWIPE_COLUMNS = [
    "StartX", "StartY", "EndX", "EndY", 
    "Length", "Duration", "BoundsArea", "MidpointPressure",
    "Q1Velocity", "Q1Pressure", "Q2Velocity", "Q2Pressure", 
    "Q3Velocity", "Q3Pressure", "Q4Velocity", "Q4Pressure"
    
]

model = None
user_table = {}


def arrange_features(swipe: dict) -> list:
    x = []
    for col in SWIPE_COLUMNS:
        x.append(swipe[col])
    return x

def collect_swipes(skip_train = True):
    global user_table
    swipe_data = fetch_data("/Swipes/")
    swipes = pd.DataFrame.from_dict(swipe_data).transpose()

    user_data = fetch_data("/Users/")
    users = pd.DataFrame.from_dict(user_data).transpose()

    swipes_X = []
    swipes_y = []
    user_swipe_counts = []

    for index, swipe in swipes.iterrows():
        id = int(swipe["UserID"])
        # id = correct_id(swipe.name, id)
        while len(user_swipe_counts) <= id:
            user_swipe_counts.append(0)
        x = arrange_features(swipe)
        swipes_X.append(x)
        swipes_y.append(id)
        user_swipe_counts[id] += 1

    print(f"Collected {len(swipes_y)} examples")
    print(user_swipe_counts)

    training_due = False

    for index, user in users.iterrows():
        if not user["modelComplete"]:
            if user_swipe_counts[user["userID"]] > LEARN_THRESHOLD:
                training_due = True
                db.reference(f"/Users/{user.name}/modelComplete").set(True)

    if training_due and not skip_train:
        train(swipes_X, swipes_y)
        
def train(X, y):
    global model

    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2)
    
    model = KNeighborsClassifier(n_neighbors=5)
    model.fit(np.array(X_train), np.array(y_train))

    acc = model.score(X_test, y_test)
    print(f"Accuracy={acc*100}%")

app = Flask(__name__)

@app.route('/auth/', methods=['POST'])
def auth():
    body = request.get_json()
    print(body)
    # features = arrange_features(body)
    # pred = model.predict(np.array([features]))

    # foundUser = db.reference("/Users/").order_by_child("userID").equal_to(body["UserID"]).get()
    # user_record_id = list(foundUser.keys())[0]

    # invalid_swipes = foundUser[user_record_id]["invalidCounts"]

    # if pred != body["UserID"]:
    #     invalid_swipes += 1

    #     if invalid_swipes >= MAX_INVALID_SWIPES:
    #         db.reference(f"/Users/{user_record_id}").update({"invalidCounts": 0})
    #         return {"keepAuth": False, "status": 200}
    #     print(f"{foundUser[user_record_id]['username']} performed an unrecognized swipe")
    
    # else:
    #     invalid_swipes -= 1
    #     invalid_swipes = max(0, invalid_swipes)

    # db.reference(f"/Users/{user_record_id}").update({"invalidCounts": invalid_swipes})
    return {"keepAuth": True, "status": 200}

def main():
    collect_swipes(False)
    # train()
    db.reference("/Swipes/").listen(collect_swipes)
    app.run(port=5001)
    
if __name__ == "__main__":
    main()
