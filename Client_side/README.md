# Funcionalities
### Showing digital map
Digital map view using OpenStreetMap database showing current location of the device, destination location (address from search bar) and routes between them. Can show any saved locations using custom markers.

<img src="UI_design/main_screen.jpg" alt="Main screen showing current location" width="300">

### Statistical analysis
Visual display of descriptive statistics using graphs (image on the left) and comparison of multiple selected routes using statistical tests (image on the right).

<img src="UI_design/graph_screen.jpg" alt="Graph showing descriptive statistics" width="300"> <img src="UI_design/tests_screen.jpg" alt="Results of statistical tests" width="300">

### Recording videos for database expansion 
Recording videos for database expansion using CameraX library. Videos are limited both with quality and time (every video is 15 seconds long) so every video has equal amount of frames. Videos are automatically uploaded to Firebase Storage with information needed for classification (geopoint, date, day of the week, time of the day).

<img src="UI_design/camera_screen.jpg" alt="Recording screen" width="300"> <img src="UI_design/fragment_screen.jpg" alt="Fragment shown after successful loading of video to Firebase" width="300">

### Setting reminders
Reminders are created manually (image on the right) by the user and can be deleted. Reminders are shown as push notifications.

<img src="UI_design/reminder_list_screen.jpg" alt="Created reminders list" width="300"> <img src="UI_design/reminder_setup_screen.jpg" alt="New reminder setup" width="300">
