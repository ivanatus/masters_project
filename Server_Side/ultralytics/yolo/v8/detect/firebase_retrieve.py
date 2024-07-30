#provide path to globals.py file
import sys
import os
linux_path = os.path.expanduser("~/masters_project/Server_Side/ultralytics/yolo/v8/detect/deep_sort_pytorch/deep_sort/sort") #update with the correct path
sys.path.append(linux_path)
from globals import Globals
import csv
import subprocess

#Firebase setup
import firebase_admin
from firebase_admin import credentials
cred = credentials.Certificate("serviceAccountKey.json")
#initialization of relevant Firebase services (storage and database)
firebase_admin.initialize_app(cred, {
    'databaseURL': 'gs://mastersproject-634d8.appspot.com/Videos',
    'storageBucket': 'mastersproject-634d8.appspot.com'
})
from firebase_admin import storage
from firebase_admin import db

def create_directory(directory_path):
    """Create "video" folder which will contain all videos downloaded from Firebase"""
    try:
        os.makedirs(directory_path)
        print(f"Directory '{directory_path}' created successfully.")
    except OSError as e:
        print(f"Error creating directory '{directory_path}': {e}")

def get_video_from_database():
    """Retrieve a video file from Firebase Cloud Storage."""
    bucket = storage.bucket() #reference to Firebase Storage
    folder_path = "Videos/"
    blobs = bucket.list_blobs(prefix=folder_path) #files in Firebase Storage

    found = False
    unanalyzed = True

    #has to be changed dependent on computer from which the code is being run
    video_directory = "video/"

    #download all videos from Firebase Storage to video folder and delete them from Firebase
    for blob in blobs:    
        filename = os.path.basename(blob.name)
        print("Downloading file:", filename)

        destination_file_path = os.path.join(video_directory, os.path.basename(blob.name))
        # Download the file
        blob.download_to_filename(destination_file_path)

        found = True
        print("Deleting file:", os.path.basename(blob.name))
        #blob.delete() #delete retrieved video from Firebase storage

    #if there is no new videos in Firebase Storage, set flag to false
    if not found: 
        print("No new videos in storage.")
        #db_ref = db.reference('Unanalyzed')
        #db_ref.set(False)
    
    call_yolo_script()

def call_yolo_script():
    """Run predict.py on all of the videos from video folder"""
    print("In call_yolo_script.")
    predict_script_path = "/home/ivana/masters_project/Server_Side/ultralytics/yolo/v8/detect/predict.py"  #update with the correct path
    predict_command = [
        "python3",
        predict_script_path,
        "model=yolov8l.pt",
        "show=False",
        "source=video"
    ]

    subprocess.run(predict_command)

if __name__ == "__main__":
    # Specify the directory path - computer dependent
    directory_path = "video/"
    create_directory(directory_path)
    get_video_from_database()