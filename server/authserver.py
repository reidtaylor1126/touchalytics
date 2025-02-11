from flask import Flask, jsonify, request
import firebase_admin
from firebase_admin import credentials, db
from collections import deque
import pandas as pd
import numpy as np

def fetch_data(table: str):
    setup_firebase()
    ref = db.reference(table)
    data = ref.get()
    return data

def fetch_users():
    data = fetch_data('/Users/')
    user_df = pd.DataFrame.from_dict(data)
    return user_df

def setup_firebase():
    cred_obj = credentials.Certificate('secret.json')
    url = 'https://touchalytics-example-default-rtdb.firebaseio.com/'

    if not firebase_admin._apps:
        firebase_admin.initialize_app(cred_obj, {'databaseURL': url})

app = Flask(__name__)
response_tracker = deque(maxlen=5)

users = fetch_users()

@app.route("/login", methods=["POST"])
def check_login():
    if request.headers.get('Content-Type') != 'application/json':
        return 'Content-Type not supported!'
    json = request.json
    userhash = hash(json["username"])

    db.reference('/Users/').