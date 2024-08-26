# Ultralytics YOLO 🚀, GPL-3.0 license

from re import split
from sre_constants import ANY_ALL
from statistics import mean, median, stdev
import hydra
import torch
import argparse
import time
from pathlib import Path

import cv2
import torch
import torch.backends.cudnn as cudnn
from numpy import random
from ultralytics.yolo.engine.predictor import BasePredictor
from ultralytics.yolo.utils import DEFAULT_CONFIG, ROOT, ops
from ultralytics.yolo.utils.checks import check_imgsz
from ultralytics.yolo.utils.plotting import Annotator, colors, save_one_box

import cv2
from deep_sort_pytorch.utils.parser import get_config
from deep_sort_pytorch.deep_sort import DeepSort
from collections import deque
import numpy as np

import sys
import os
linux_path = os.path.expanduser("~/masters_project/Server_Side/ultralytics/yolo/v8/detect/deep_sort_pytorch/deep_sort/sort/") #update with the correct path
sys.path.append(linux_path)
import csv

from globals import Globals
import pandas as pd
import matplotlib.pyplot as plt
from scipy.stats import shapiro

#Firebase setup
import firebase_admin
from firebase_admin import credentials
cred = credentials.Certificate("serviceAccountKey.json")
#firebase_admin.initialize_app(cred)
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://mastersproject-634d8-default-rtdb.europe-west1.firebasedatabase.app/',
    'storageBucket': 'mastersproject-634d8.appspot.com'
})
from firebase_admin import storage
from firebase_admin import db


palette = (2 ** 11 - 1, 2 ** 15 - 1, 2 ** 20 - 1)
data_deque = {}

deepsort = None

global_instance = Globals()

def init_tracker():

    
    with open(global_instance.filename + '_per_frame.csv', 'a', newline='') as csvfile:
            fieldnames = ['frame', 'people', 'buses', 'cars', 'trucks', 'bikes', 'trains', 'motorbike']
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            writer.writerow({'frame': "Frame", 'people': "People", 'buses': "Buses", 'cars': "Cars", 'trucks': "Trucks", 'bikes': "Bikes", 'trains': "Trains", "motorbike": "Motorbikes"})

    with open(global_instance.filename + '_vehicles_ids.csv', 'a', newline='') as csvfile:
            fieldnames = ['frame', 'id', 'type']
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            writer.writerow({'frame': "Frame", 'id': "ID", 'type': "Type"})

    global deepsort

    cfg_deep = get_config()
    cfg_deep.merge_from_file("deep_sort_pytorch/configs/deep_sort.yaml")

    deepsort= DeepSort(cfg_deep.DEEPSORT.REID_CKPT,
                            max_dist=cfg_deep.DEEPSORT.MAX_DIST, min_confidence=cfg_deep.DEEPSORT.MIN_CONFIDENCE,
                            nms_max_overlap=cfg_deep.DEEPSORT.NMS_MAX_OVERLAP, max_iou_distance=cfg_deep.DEEPSORT.MAX_IOU_DISTANCE,
                            max_age=cfg_deep.DEEPSORT.MAX_AGE, n_init=cfg_deep.DEEPSORT.N_INIT, nn_budget=cfg_deep.DEEPSORT.NN_BUDGET,
                            use_cuda=True)


def xyxy_to_xywh(*xyxy):
    """" Calculates the relative bounding box from absolute pixel values. """
    bbox_left = min([xyxy[0].item(), xyxy[2].item()])
    bbox_top = min([xyxy[1].item(), xyxy[3].item()])
    bbox_w = abs(xyxy[0].item() - xyxy[2].item())
    bbox_h = abs(xyxy[1].item() - xyxy[3].item())
    x_c = (bbox_left + bbox_w / 2)
    y_c = (bbox_top + bbox_h / 2)
    w = bbox_w
    h = bbox_h
    return x_c, y_c, w, h

def xyxy_to_tlwh(bbox_xyxy):
    tlwh_bboxs = []
    for i, box in enumerate(bbox_xyxy):
        x1, y1, x2, y2 = [int(i) for i in box]
        top = x1
        left = y1
        w = int(x2 - x1)
        h = int(y2 - y1)
        tlwh_obj = [top, left, w, h]
        tlwh_bboxs.append(tlwh_obj)
    return tlwh_bboxs

def compute_color_for_labels(label):
    """
    Simple function that adds fixed color depending on the class
    """
    if label == 0: #person
        color = (85,45,255)
    elif label == 2: #car
        color = (222,82,175)
    elif label == 3:  #motorbike
        color = (0, 204, 255)
    elif label == 5:  #bus
        color = (0, 149, 255)
    elif label == 1: #bike
        color = (0, 0, 255)
    elif label == 7: #truck
        color = (255, 0, 0)
    elif label == 9: #train
        color = (0, 255, 0)
    else:
        color = [int((p * (label ** 2 - label + 1)) % 255) for p in palette]
    return tuple(color)

def draw_border(img, pt1, pt2, color, thickness, r, d):
    x1,y1 = pt1
    x2,y2 = pt2
    # Top left
    cv2.line(img, (x1 + r, y1), (x1 + r + d, y1), color, thickness)
    cv2.line(img, (x1, y1 + r), (x1, y1 + r + d), color, thickness)
    cv2.ellipse(img, (x1 + r, y1 + r), (r, r), 180, 0, 90, color, thickness)
    # Top right
    cv2.line(img, (x2 - r, y1), (x2 - r - d, y1), color, thickness)
    cv2.line(img, (x2, y1 + r), (x2, y1 + r + d), color, thickness)
    cv2.ellipse(img, (x2 - r, y1 + r), (r, r), 270, 0, 90, color, thickness)
    # Bottom left
    cv2.line(img, (x1 + r, y2), (x1 + r + d, y2), color, thickness)
    cv2.line(img, (x1, y2 - r), (x1, y2 - r - d), color, thickness)
    cv2.ellipse(img, (x1 + r, y2 - r), (r, r), 90, 0, 90, color, thickness)
    # Bottom right
    cv2.line(img, (x2 - r, y2), (x2 - r - d, y2), color, thickness)
    cv2.line(img, (x2, y2 - r), (x2, y2 - r - d), color, thickness)
    cv2.ellipse(img, (x2 - r, y2 - r), (r, r), 0, 0, 90, color, thickness)

    cv2.rectangle(img, (x1 + r, y1), (x2 - r, y2), color, -1, cv2.LINE_AA)
    cv2.rectangle(img, (x1, y1 + r), (x2, y2 - r - d), color, -1, cv2.LINE_AA)
    
    cv2.circle(img, (x1 +r, y1+r), 2, color, 12)
    cv2.circle(img, (x2 -r, y1+r), 2, color, 12)
    cv2.circle(img, (x1 +r, y2-r), 2, color, 12)
    cv2.circle(img, (x2 -r, y2-r), 2, color, 12)
    
    return img

def UI_box(x, img, color=None, label=None, line_thickness=None):
    # Plots one bounding box on image img
    tl = line_thickness or round(0.002 * (img.shape[0] + img.shape[1]) / 2) + 1  # line/font thickness
    color = color or [random.randint(0, 255) for _ in range(3)]
    c1, c2 = (int(x[0]), int(x[1])), (int(x[2]), int(x[3]))
    cv2.rectangle(img, c1, c2, color, thickness=tl, lineType=cv2.LINE_AA)
    if label:
        tf = max(tl - 1, 1)  # font thickness
        t_size = cv2.getTextSize(label, 0, fontScale=tl / 3, thickness=tf)[0]

        img = draw_border(img, (c1[0], c1[1] - t_size[1] -3), (c1[0] + t_size[0], c1[1]+3), color, 1, 8, 2)

        cv2.putText(img, label, (c1[0], c1[1] - 2), 0, tl / 3, [225, 255, 255], thickness=tf, lineType=cv2.LINE_AA)


def draw_boxes(img, bbox, names,object_id, identities=None, offset=(0, 0)):
    #cv2.line(img, line[0], line[1], (46,162,112), 3)

    height, width, _ = img.shape
    # remove tracked point from buffer if object is lost
    for key in list(data_deque):
      if key not in identities:
        data_deque.pop(key)

    for i, box in enumerate(bbox):
        x1, y1, x2, y2 = [int(i) for i in box]
        x1 += offset[0]
        x2 += offset[0]
        y1 += offset[1]
        y2 += offset[1]

        # code to find center of bottom edge
        center = (int((x2+x1)/ 2), int((y2+y2)/2))

        # get ID of object
        id = int(identities[i]) if identities is not None else 0

        # create new buffer for new object
        if id not in data_deque:  
          data_deque[id] = deque(maxlen= 64)
        color = compute_color_for_labels(object_id[i])
        obj_name = names[object_id[i]]
        label = '{}{:d}'.format("", id) + ":"+ '%s' % (obj_name)

        if id not in global_instance.vehicle_ids:
            global_instance.vehicle_ids.append(id)
            write_ids(id, obj_name)
            print(global_instance.vehicle_ids)
        
        if(obj_name not in ['person', 'bike', 'bus', 'car', 'train', 'truck', 'motorbike']):
            return None
            
        # add center to buffer
        data_deque[id].appendleft(center)
        UI_box(box, img, label=label, color=color, line_thickness=2)
        # draw trail
        for i in range(1, len(data_deque[id])):
            # check if on buffer value is none
            if data_deque[id][i - 1] is None or data_deque[id][i] is None:
                continue
            # generate dynamic thickness of trails
            thickness = int(np.sqrt(64 / float(i + i)) * 1.5)
            # draw trails
            cv2.line(img, data_deque[id][i - 1], data_deque[id][i], color, thickness)
    return img

def write_ids(id, type):
    with open(global_instance.filename + '_vehicles_ids.csv', 'a', newline='') as csvfile:
            fieldnames = ['frame', 'id', 'type']
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            writer.writerow({'frame': global_instance.global_frame, 'id': id, 'type': type})
    with open('overall.csv', 'a', newline='') as csvfile:
            fieldnames = ['frame', 'id', 'type']
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            writer.writerow({'frame': global_instance.global_frame, 'id': id, 'type': type})

    

class DetectionPredictor(BasePredictor):

    def get_annotator(self, img):
        return Annotator(img, line_width=self.args.line_thickness, example=str(self.model.names))

    def preprocess(self, img):
        img = torch.from_numpy(img).to(self.model.device)
        img = img.half() if self.model.fp16 else img.float()  # uint8 to fp16/32
        img /= 255  # 0 - 255 to 0.0 - 1.0
        return img

    def postprocess(self, preds, img, orig_img):
        preds = ops.non_max_suppression(preds,
                                        self.args.conf,
                                        self.args.iou,
                                        agnostic=self.args.agnostic_nms,
                                        max_det=self.args.max_det)

        # Initialize counters for each class
        num_people = 0
        num_cars = 0
        num_bikes = 0
        num_buses = 0
        num_trucks = 0
        num_trains = 0
        num_motors = 0


        for i, pred in enumerate(preds):
            for det in pred:
                label = int(det[5])
                if label not in [0, 2, 3, 5, 7, 9]:
                    if preds.__contains__(pred):
                        preds.remove(pred)
                else:
                     # Increment the respective counter based on the label
                    if label == 0:
                        num_people += 1
                    elif label == 1:
                        num_bikes += 1
                    elif label == 2:
                        num_cars += 1
                    elif label == 3:
                        num_motors += 1
                    elif label == 5:
                        num_buses += 1
                    elif label == 7:
                        num_trucks += 1
                    elif label == 9:
                        num_trains += 1

            shape = orig_img[i].shape if self.webcam else orig_img.shape
            pred[:, :4] = ops.scale_boxes(img.shape[2:], pred[:, :4], shape).round()

        global_instance.no_of_people=num_people
        global_instance.no_of_cars=num_cars
        global_instance.no_of_bikes=num_bikes
        global_instance.no_of_buses=num_buses
        global_instance.no_of_trucks=num_trucks
        global_instance.no_of_trains=num_trains
        global_instance.no_of_motors=num_motors

        # Return the filtered detections and the counts of each class
        return preds

    def write_results(self, idx, preds, batch):
        p, im, im0 = batch
        all_outputs = []
        log_string = ""
        if len(im.shape) == 3:
            im = im[None]  # expand for batch dim
        self.seen += 1
        im0 = im0.copy()
        if self.webcam:  # batch_size >= 1
            log_string += f'{idx}: '
            frame = self.dataset.count
            global_instance.set_global_frame(frame)
        else:
            frame = getattr(self.dataset, 'frame', 0)
            global_instance.set_global_frame(frame)

        self.data_path = p
        save_path = str(self.save_dir / p.name)  # im.jpg
        self.txt_path = str(self.save_dir / 'labels' / p.stem) + ('' if self.dataset.mode == 'image' else f'_{frame}')
        log_string += '%gx%g ' % im.shape[2:]  # print string
        self.annotator = self.get_annotator(im0)

        if idx >= len(preds):
            global_instance.set_no_of_people(0)
            print("Nista nije detektirano u frame-u " + str(global_instance.get_global_frame()))
            return log_string
        
        with open(global_instance.filename + '_per_frame.csv', 'a', newline='') as csvfile:
            fieldnames = ['frame', 'people', 'buses', 'cars', 'trucks']
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            writer.writerow({'frame': global_instance.global_frame, 'people': global_instance.no_of_people, 'buses': global_instance.no_of_buses, 'cars': global_instance.no_of_cars, 'trucks': global_instance.no_of_trucks})

        det = preds[idx]
        all_outputs.append(det)
        if len(det) == 0:
            return log_string
        for c in det[:, 5].unique():
            n = (det[:, 5] == c).sum()  # detections per class
            ##################################################################
            """global_instance.set_no_of_people(f"{n}")
            check_string = f"{n} {self.model.names[int(c)]}{'s' * (n > 1)}"
            string_parts = check_string.split(' ')
            if string_parts[1] == 'person' or string_parts[1] == 'persons':
                log_string += f"{n} {self.model.names[int(c)]}{'s' * (n > 1)}, """
            ##################################################################
            log_string += f"{n} {self.model.names[int(c)]}{'s' * (n > 1)}, "

        # write
        gn = torch.tensor(im0.shape)[[1, 0, 1, 0]]  # normalization gain whwh
        xywh_bboxs = []
        confs = []
        oids = []
        outputs = []
        for *xyxy, conf, cls in reversed(det):
            x_c, y_c, bbox_w, bbox_h = xyxy_to_xywh(*xyxy)
            xywh_obj = [x_c, y_c, bbox_w, bbox_h]
            xywh_bboxs.append(xywh_obj)
            confs.append([conf.item()])
            oids.append(int(cls))
        xywhs = torch.Tensor(xywh_bboxs)
        confss = torch.Tensor(confs)
          
        outputs = deepsort.update(xywhs, confss, oids, im0)
        if len(outputs) > 0:
            bbox_xyxy = outputs[:, :4]
            identities = outputs[:, -2]
            object_id = outputs[:, -1]
            
            draw_boxes(im0, bbox_xyxy, self.model.names, object_id,identities)

        return log_string

""""
@hydra.main(version_base=None, config_path=str(DEFAULT_CONFIG.parent), config_name=DEFAULT_CONFIG.name)
def predict(cfg, filename):
    init_tracker()
    cfg.model = cfg.model or "yolov8n.pt"
    cfg.imgsz = check_imgsz(cfg.imgsz, min_dim=2)  # check image size
    #cfg.source = cfg.source if cfg.source is not None else ROOT / "assets"
    cfg.source = filename
    predictor = DetectionPredictor(cfg)
    predictor()"""

@hydra.main(config_path=str(DEFAULT_CONFIG.parent), config_name=DEFAULT_CONFIG.name, version_base=None)
def predict(cfg):
    print("in hydra main predict,py")
    init_tracker()
    cfg.model = cfg.model or "yolov8n.pt"
    cfg.imgsz = check_imgsz(cfg.imgsz, min_dim=2)  # check image size
    # cfg.source is the directory containing the video files
    source_directory = hydra.utils.to_absolute_path(cfg.source)
    print(source_directory)
    for filename in os.scandir(source_directory):
        print(filename)
        if filename.is_file():
            predictor = DetectionPredictor(cfg)
            predictor(filename.path)
            global_instance.filename = filename.name
            info = filename.name.split('_')
            print(f"Latitude: {info[0]}")
            print(f"Longitude: {info[1]}")
            print(f"Date: {info[2]}")
            print(f"Time: {info[3]}")
            global_instance.processed_files.append(global_instance.filename)

"""
Function that is called at the very end of execution. Analyses and plots collected data.
TO DO
"""
def analyze_and_send(info):
    if os.path.exists(global_instance.filename + '_vehicles_ids.csv'):
        df_vehicle_ids = pd.read_csv(global_instance.filename + '_vehicles_ids.csv')
    else:
        df_vehicle_ids = pd.read_csv('_vehicles_ids.csv')

    latitude = info[0]
    longitude = info[1]
    date = info[2]
    day = info[3]
    time = info[4]
    time = time.split('.')
    time = time[0]

    types_of_vehicles = df_vehicle_ids.iloc[:, 2].values 
    cars_overall = 0
    trucks_overall = 0
    buses_overall = 0
    trains_overall = 0
    bikes_overall = 0
    motorbikes_overall = 0
    people_overall = 0

    for type in types_of_vehicles:
        if type == 'car':
            cars_overall += 1
        elif type == 'truck':
            trucks_overall += 1
        elif type == 'bus':
            buses_overall += 1
        elif type == 'motorbike':
            motorbikes_overall += 1
        elif type == 'train':
            trains_overall += 1
        elif type == 'bike':
            bikes_overall += 1
        elif type == 'person':
            people_overall += 1

    latitude = float(latitude)
    latitude = round(latitude, 3)
    longitude = float(longitude)
    longitude = round(longitude, 3)
    time = time.split(':')
    interval = ""
    time = list(map(int, time))
    if time[1] < 30:
        #interval = time[0] + ":00-" + time[0] + ":30"
        interval = f"{time[0]}:00-{time[0]}:30"
        print(f"Interval: {interval}")
    else:
        #interval = time[0] + ":30-"
        interval = f"{time[0]}:30-"
        time[0] += 1
        #interval = interval + time[0] + ":00"
        interval += f"{str(time[0])}:00"
        print(f"Interval: {interval}")

    bucket = storage.bucket() #reference to Firebase Storage
    folder_path = "Overall_detections_days/" + day + "/" + f"{latitude}_{longitude}/"
    print(f"folder path: {folder_path}")
    blobs = bucket.list_blobs(prefix=folder_path) #files in Firebase Storage
    found = False

    if not blobs:
        print("Direktorij još ne postoji.")
    else:
        for blob in blobs:    
            filename = os.path.basename(blob.name)
            filename = filename.split('.')

            if(filename[0] == interval):
                print("Rezultati nadeni")
                blob.download_to_filename("old_overall.csv")
                print(blob.name)
                found = True
                #blob.delete()
                break

    if not found:
        print("Rezultati nisu nadeni")
        with open('new_overall.csv', 'a', newline='') as csvfile:
            fieldnames = ['Cars', 'Buses', 'Trucks', 'Trains', 'People', 'Bikes', 'Motorbikes']
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            writer.writerow({'Cars': 'Cars', 'Buses': 'Buses', 'Trucks': 'Trucks', 'Trains': 'Trains', 'People': 'People', 'Bikes': 'Bikes', 'Motorbikes': 'Motorbikes'})
            writer.writerow({'Cars': cars_overall, 'Buses': buses_overall, 'Trucks': trucks_overall, 'Trains': trains_overall, 'People': people_overall, 'Bikes': bikes_overall, 'Motorbikes': motorbikes_overall})
    
    if found:
        with open('old_overall.csv', 'a', newline='') as csvfile:
            fieldnames = ['Cars', 'Buses', 'Trucks', 'Trains', 'People', 'Bikes', 'Motorbikes']
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            writer.writerow({'Cars': cars_overall, 'Buses': buses_overall, 'Trucks': trucks_overall, 'Trains': trains_overall, 'People': people_overall, 'Bikes': bikes_overall, 'Motorbikes': motorbikes_overall})

        df_overall = pd.read_csv("old_overall.csv")
        people = df_overall.iloc[:, 4].values
        cars = df_overall.iloc[:, 0].values 
        buses = df_overall.iloc[:, 1].values 
        trucks = df_overall.iloc[:, 2].values
        bikes = df_overall.iloc[:, 5].values
        trains = df_overall.iloc[:, 3].values
        motorbikes = df_overall.iloc[:, 6].values

        car_mean = mean(cars)
        car_median = median(cars)
        car_std = stdev(cars)
        buses_mean = mean(buses)
        buses_median = median(buses)
        bus_std = stdev(buses)
        trucks_mean = mean(trucks)
        trucks_median = median(trucks)
        truck_std = stdev(trucks)
        train_mean = mean(trains)
        train_median = median(trains)
        train_std = stdev(trains)
        bikes_mean = mean(bikes)
        bikes_median = median(bikes)
        bikes_std = stdev(bikes)
        motorbikes_mean = mean(motorbikes)
        motorbikes_median = median(motorbikes)
        motorbikes_std = stdev(motorbikes)
        people_mean = mean(people)
        people_median = median(people)
        people_std = stdev(people)

        car_stats, car_p_value = shapiro(cars)
        bus_stats, bus_p_value = shapiro(buses)
        truck_stats, truck_p_value = shapiro(trucks)
        train_stats, train_p_value = shapiro(trains)
        people_stats, people_p_value = shapiro(people)
        motorbikes_stats, motorbikes_p_value = shapiro(motorbikes)
        bikes_stats, bikes_p_value = shapiro(bikes)

    overall_dets = ""
    if found:
        overall_dets = "old_overall.csv"
    else:
        overall_dets = "new_overall.csv"

    destination_file_path = "Overall_detections_days/" + day + "/" + f"{latitude}_{longitude}/" + interval + ".csv"
    print(destination_file_path)
    upload_file = bucket.blob(destination_file_path)
    upload_file.upload_from_filename(overall_dets)

    latitude_str = str(latitude).replace(".", "_")
    longitude_str = str(longitude).replace(".", "_")

    # Replace colons with underscores in the interval
    interval = interval.replace(":", "_")

    db_path = f"{latitude_str}-{longitude_str}/{day}/{interval}"


    # Read descriptive_stats.csv and upload to Realtime Database
    descriptive_data = {
    'mean_car': float(car_mean),
    'med_car': float(car_median),
    'std_car': float(car_std),
    'cars_shapiro': float(car_p_value),
    'mean_bus': float(buses_mean),
    'med_bus': float(buses_median),
    'std_bus': float(bus_std),
    'bus_shapiro': float(bus_p_value),
    'mean_truck': float(trucks_mean),
    'med_truck': float(trucks_median),
    'std_truck': float(truck_std),
    'truck_shapiro': float(truck_p_value),
    'mean_ppl': float(people_mean),
    'med_ppl': float(people_median),
    'std_ppl': float(people_std),
    'ppl_shapiro': float(people_p_value),
    'mean_train': float(train_mean),
    'med_train': float(train_median),
    'std_train': float(train_std),
    'train_shapiro': float(train_p_value),
    'mean_bike': float(bikes_mean),
    'med_bike': float(bikes_median),
    'std_bike': float(bikes_std),
    'bike_shapiro': float(bikes_p_value),
    'mean_motor': float(motorbikes_mean),
    'med_motor': float(motorbikes_median),
    'std_motor': float(motorbikes_std),
    'motor_shapiro': float(motorbikes_p_value)
}

    # Reference and set data in Realtime Database
    ref = db.reference(db_path)
    ref.set(descriptive_data)

    if os.path.exists(overall_dets):
        os.remove(overall_dets)
    else:
        print(f"{overall_dets} does not exist in the current directory.")


    if os.path.exists(global_instance.filename + '_per_frame.csv'):
        os.remove(global_instance.filename + "_per_frame.csv")
    else:
        os.remove("_per_frame.csv")

    if os.path.exists(global_instance.filename + '_vehicles_ids.csv'):
        os.remove(global_instance.filename + "_vehicles_ids.csv")
    else:
        os.remove("_vehicles_ids.csv")

    if os.path.exists('overall.csv'):
        os.remove("overall.csv")
    else:
        os.remove("overall.csv")

if __name__ == "__main__":
    print("In predict.py")
    with open('overall.csv', 'a', newline='') as csvfile:
            fieldnames = ['frame', 'id', 'type']
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            writer.writerow({'frame': "Frame", 'id': "ID", 'type': "Type"})
    
    with open('descriptive_stats.csv', 'a', newline='') as csvfile:
            fieldnames = ['mean_car', 'med_car', 'std_car', 'cars_overall', 'mean_bus', 'med_bus', 'std_bus', 'bus_overall', 'mean_truck', 'med_truck', 'std_truck', 'truck_overall']
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            writer.writerow({'mean_car': "mean_car", 'med_car': "med_car", 'std_car': "std_car", 'cars_overall': "cars_overall", 'mean_bus': "mean_bus", 'med_bus': "med_bus", 'std_bus': "std_bus",'bus_overall': "bus_overall", 'mean_truck': "mean_truck", 'med_truck': "med_truck", 'std_truck': "std_truck", 'truck_overall': "truck_overall"})

    predict()

    for file_name in global_instance.processed_files:
        global_instance.filename = file_name
        print(f"Attempting to read file: {file_name}")
        info = file_name.split('_')
        print(f"Latitude: {info[0]}")
        print(f"Longitude: {info[1]}")
        print(f"Date: {info[2]}")
        print(f"Day: {info[3]}")
        print(f"Time: {info[4]}")
        analyze_and_send(info)