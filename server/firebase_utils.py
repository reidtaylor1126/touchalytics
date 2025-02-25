
import firebase_admin
from firebase_admin import credentials, db

def fetch_data(path: str):
    setup_firebase()
    ref = db.reference(path)
    data = ref.get()
    return data

def setup_firebase():
    cred_obj = credentials.Certificate('secret.json')
    url = 'https://touchalytics-example-default-rtdb.firebaseio.com/'

    if not firebase_admin._apps:
        firebase_admin.initialize_app(cred_obj, {'databaseURL': url})