# Projekt 2: Kodiranje i kriptografija
Analiza prometa putem video snimki koristeći YOLOv8 model. 
Korišten je javno dostupan dataset preuzet sa stranice https://www.kaggle.com/datasets/aryashah2k/highway-traffic-videos-dataset?fbclid=IwAR25M6RH8IJU2zIvbaxQllpTlrDqMWqwaVt2a610DgTheplERNs4G4hWAII.
Pokretano na Linux Ubuntu 22.04.3 OS-u, AMD Ryzen 7 procesoru s AMD Radeon RENOIR grafičkom karticom.

## Instalacija i pokretanje programa
- Kloniranje repozitorija
```
git clone https://github.com/ivanatus/KIK_projekt
```
- Promjena direktorija
```
cd KIK_projekt
```
- Instalacija potrebnih knjižica
```
pip install -e '.[dev]'
pip install pingouin
```
ili u slučaju rada na Windowsima:
```
pip install -r requirements.txt
pip install pingouin
```

- Prelazak u daljnji direktorij
```
cd ultralytics/yolo/v8/detect
```
- Preuzimanje DeepSort datoteka
```
https://drive.google.com/drive/folders/1kna8eWGrSfzaR6DtNJ8_GchGgPMv3VC8?usp=sharing
```
- Izdvajanje datoteka iz zip i pozicioniranje deep_sort_pytorch foldera u yolo/v8/detect folder

- Pokretanje koda u teminalu (command prompt-u, powershell prompt-u, anaconda promt-u...)

```
python predict.py model=yolov8l.pt show=False source=video
python analysis.py
```
ili u slučaju javljene greške

```
python3 predict.py model=yolov8l.pt show=False source=video
python3 analysis.py
```
