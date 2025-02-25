import requests
from firebase_utils import *
from firebase_admin import db
import pandas as pd

setup_firebase()

sw = db.reference("/Swipes/-OJMf5k1-3AqyMCSDJds").get()
# sw["UserID"] = 1

print(sw)