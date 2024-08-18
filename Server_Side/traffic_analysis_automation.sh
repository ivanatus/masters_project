#!/bin/bash

# Path to Python script, should be customized for each computer
SCRIPT_PATH="/home/ivana/masters_project/Server_Side/ultralytics/yolo/v8/detect/firebase_retrieve.py"

# Schedule the cron job at 19:00 every day
(crontab -l 2>/dev/null; echo "0 19 * * * /usr/bin/python3 $SCRIPT_PATH") | crontab -
