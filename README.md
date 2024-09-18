# Projekt za diplomski rad

Ovaj rad istražuje primjenu računalnog vida za automatizaciju analize prometa. Korišten je prethodno treniran YOLO model za detekciju objekata. Statistička analiza odrađena je nad rezultatima detekcije objekata računalnim vidom u dva dijela: deskriptivna statistika koristeći Python programski jezik i NumPy knjižicu te inferencijalna statistika unutar Android aplikacije pomoću Apache Commons Math knjižice. Deskriptivna statistika uključuje izračun mjera centralne tendencije, dok se inferencijalna statistika fokusira na usporedbu prometa na različitim lokacijama pomoću statističkih testova. Unutar Android aplikacije grafički su prikazani rezultati statističke analize kako bi se prikazalo statistički vjerojatno trenutno stanje prometa. Aplikacija također uključuje digitalnu kartu s OpenStreetMap-om i koristi Firebase platformu za pohranu podataka i komunikaciju između Android aplikacije i poslužitelja, te ima mogućnost direktnog slanja video snimki u bazu podataka kako bi ih poslužiteljska strana obradila.

## Tehnologije
### Poslužiteljska komponenta
- predtreniran model YOLOv8 (uključujući YOLO arhitekturu: YAML, Python, pt, cfg...)
- Python 3.10 (ili viši), uključujući knjižice: Numpy, Scipy, Pingouin, Hydra, OpenCV, PyTorch, Pandas, Matplotlib, Firebase Admin
### Klijentska komponenta
- Android nativna aplikacija za Android verziju 7.0 i višu
- Java programski jezik
- OpenStreetMap za prikaz lokacija, CameraX za pristup kameri uređaja, Apache Commons Math za statističku analizu
### Integracija komponenti
- Google Firebase platforma za računarstvo u oblaku
- Firebase Realtime Database baza podataka u stvarnom vremenu JSON formata
- Firebase Storage pohrana datoteka i podataka u oblaku

# Project for master's thesis

This project explores the application of computer vision for automating traffic analysis. A pre-trained YOLO model was used for object detection. The statistical analysis was conducted on the results of object detection in two parts: descriptive statistics using the Python programming language and the NumPy library, and inferential statistics within the Android application using the Apache Commons Math library. Descriptive statistics include the calculation of measures of central tendency, while inferential statistics focus on comparing traffic at different locations using statistical tests. Within the Android application, the results of the statistical analysis are graphically displayed to show the statistically probable current traffic conditions. The application also includes a digital map using OpenStreetMap and utilizes the Firebase platform for data storage and communication between the Android application and the server. It also has the capability to directly upload video recordings to the database for processing on the server side.

## Technologies
### Server component
- pre-trained YOLOv8 (including YOLO architecture: YAML, Python, pt, cfg...)
- Python 3.10 (or higher), using modules: Numpy, Scipy, Pingouin, Hydra, OpenCV, PyTorch, Pandas, Matplotlib, Firebase Admin
### Client component
- native Android application for Android version 7.0 or higher
- Java programming language
- OpenStreetMap for digital map, CameraX to access device's camera, Apache Commons Math for statistical analysis
### Integration of components
- Google Firebase platform for cloud computing
- Firebase Realtime Database in JSON format
- Firebase Storage for storage of files and data in cloud
