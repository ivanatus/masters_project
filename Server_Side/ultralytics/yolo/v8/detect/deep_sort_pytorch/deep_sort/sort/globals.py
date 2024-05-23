# -*- coding: utf-8 -*-
"""
Create globals.py for retrieving information about frames to import in predict and detection

this file is in the same folder as detection.py
"""

class Globals:
    
    global_frame = 0
    no_of_people = 0
    no_of_cars = 0
    no_of_bikes = 0
    no_of_buses = 0
    no_of_trucks = 0
    no_of_trains = 0
    filename = ""
    vehicle_ids = []
    processed_files = []
    car_means = []
    bus_means = []
    truck_means = []
    
    @staticmethod
    def set_global_frame(frame):
        Globals.global_frame = frame
        
    @staticmethod
    def get_global_frame():
        return Globals.global_frame
    
    @staticmethod
    def set_no_of_people(n):
        Globals.no_of_people = n
        
    @staticmethod
    def get_no_of_people():
        return Globals.no_of_people
    
    @staticmethod
    def set_no_of_cars(n):
        Globals.no_of_cars = n
        
    @staticmethod
    def get_no_of_cars():
        return Globals.no_of_cars
    
    @staticmethod
    def set_no_of_bikes(n):
        Globals.no_of_bikes = n
        
    @staticmethod
    def get_no_of_bikes():
        return Globals.no_of_bikes
    
    @staticmethod
    def set_no_of_buses(n):
        Globals.no_of_buses = n
        
    @staticmethod
    def get_no_of_buses():
        return Globals.no_of_buses
    
    @staticmethod
    def set_no_of_trucks(n):
        Globals.no_of_trucks = n
        
    @staticmethod
    def get_no_of_trucks():
        return Globals.no_of_trucks
    
    @staticmethod
    def set_no_of_trains(n):
        Globals.no_of_trains = n
        
    @staticmethod
    def get_no_of_trains():
        return Globals.no_of_trains