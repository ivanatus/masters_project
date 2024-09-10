package com.example.traffic_analysis_app;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.osmdroid.util.GeoPoint;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class RecordingActivity extends AppCompatActivity implements VideoTransferFragment.OnVideoTransferListener  {

    FloatingActionButton capture_video;
    Recording recording = null;
    VideoCapture<Recorder> video_capture = null;
    PreviewView preview_view;
    BottomNavigationView bottom_navigation;
    Toolbar toolbar;
    int camera_facing = CameraSelector.LENS_FACING_BACK;
    private final ActivityResultLauncher<String> activity_result_launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            startCamera(camera_facing);
        }
    });
    GeoPoint current_location;
    private VideoTransferFragment videoTransferFragment = VideoTransferFragment.newInstance();
    boolean showToolbarMenu = false;
    Globals globals = Globals.getInstance();
    private CountDownTimer countDownTimer;
    CardView timerCardView;
    TextView timerTextView;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    private void removeOverflowMenu(Menu menu) {
        try {
            Field field = MenuBuilder.class.getDeclaredField("mOptionalIconsVisible");
            field.setAccessible(true);
            field.set(menu, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //adding the navigation bar to the layout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);

        if (!showToolbarMenu)
            return false;
        getMenuInflater().inflate(R.menu.bottom_navigation, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //defining the action that is done after navigation bar/toolbar item selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.recording) {
            Intent recording = new Intent(getApplicationContext(), RecordingActivity.class);
            startActivity(recording);
            finish();
        } else if(id == R.id.info) {
            //userInstructions(this);
            Intent data = new Intent(getApplicationContext(), InfoActivity.class);
            startActivity(data);
            finish();
        } else if(id == R.id.settings){
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            finish();
        } else if(id == R.id.home){
            Intent home = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(home);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.d("GLOBALS", "RecordingActivity latitude: " + globals.getGlobalLatitude());
        Log.d("GLOBALS", "RecordingActivity longitude: " + globals.getGlobalLongitude());

        current_location = new GeoPoint(globals.getGlobalLatitude(), globals.getGlobalLongitude());
        timerCardView = findViewById(R.id.timerCardView);
        timerTextView = findViewById(R.id.timerTextView);

        bottom_navigation = findViewById(R.id.bottom_navigation);
        // Clear any selected item
        bottom_navigation.setOnNavigationItemSelectedListener(null);
        bottom_navigation.getMenu().setGroupCheckable(0, true, false);
        for (int i = 0; i < bottom_navigation.getMenu().size(); i++) {
            bottom_navigation.getMenu().getItem(i).setChecked(false);
        }
        bottom_navigation.getMenu().setGroupCheckable(0, true, true);
        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if(id == R.id.home){
                    Intent home = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(home);
                    finish();
                } else if(id == R.id.info){
                    Intent info = new Intent(getApplicationContext(), InfoActivity.class);
                    startActivity(info);
                    finish();
                } else if(id == R.id.settings) {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent);
                    finish();
                }

                return false;
            }
        });

        Intent intent = getIntent();
        double latitude = intent.getDoubleExtra("current_location_latitude", 0);
        double longitude = intent.getDoubleExtra("current_location_longitude", 0);
        current_location = new GeoPoint(latitude, longitude);

        capture_video = findViewById(R.id.video_capture);
        preview_view = findViewById(R.id.camera_preview);

        // Initialize the timer with 15 seconds (15000 milliseconds)
        countDownTimer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Convert milliseconds to minutes and seconds
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                String timeFormatted = String.format("00:%02d", secondsRemaining);

                // Update the timer TextView
                timerTextView.setText(timeFormatted);
            }

            @Override
            public void onFinish() {
                // Set the timer text to 00:00 when finished
                timerTextView.setText("00:00");
            }
        };

        //handle click on recording button - stop or start recording
        capture_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CAMERA", "In capture_video.setOnClickListener");
                //if permissions for video recording not already given, ask for them
                if (ActivityCompat.checkSelfPermission(RecordingActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    Log.d("CAMERA", "Ask camera permission");
                    activity_result_launcher.launch(Manifest.permission.CAMERA);
                } else if (ActivityCompat.checkSelfPermission(RecordingActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                    Log.d("CAMERA", "Ask audio permission");
                    activity_result_launcher.launch(Manifest.permission.RECORD_AUDIO);
                } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ActivityCompat.checkSelfPermission(RecordingActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("CAMERA", "Ask storage permission");
                    activity_result_launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                } else {
                    captureVideo();
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d("CAMERA", "Ask camera permission");
            activity_result_launcher.launch(Manifest.permission.CAMERA);
        } else {
            Log.d("CAMERA", "Has camera permission");
            startCamera(camera_facing);
        }
    }

    //method to handle capturing video recording
    private void captureVideo() {
        //capture_video.setImageResource(R.drawable.camera_pause); //change button image
        capture_video.setVisibility(View.GONE);
        startTimer();
        Recording recording1 = recording;
        Log.d("Delete video", "captureVideo recording = " + String.valueOf(recording1));
        if (recording1 != null) { //stop the recording
            recording1.stop();
            stopTimer();
            //sendToFirebase(recording);
            recording = null;
            return;
        }
        //save video recording to the device - name format is uid_year_month_day_hour_minute_second_millisecond.mp4
        String name = "n";
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/traffic_analysis");

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues).build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //get type of action - starting or stopping the recording of the video
        recording = video_capture.getOutput().prepareRecording(RecordingActivity.this, options).withAudioEnabled().start(ContextCompat.getMainExecutor(RecordingActivity.this), videoRecordEvent -> {
            if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                Toast.makeText(this, "RECORDING STARTED", Toast.LENGTH_SHORT).show();
                capture_video.setEnabled(true); //start recording
                // Stop recording after 15 seconds
                new Handler().postDelayed(() -> {
                    if (recording != null) {
                        recording.stop();
                        recording = null;
                        capture_video.setEnabled(true); // Enable the button after recording stops
                    }
                }, 15000); // 15000 milliseconds = 15 seconds
            } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) { //stop recording
                if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                    Toast.makeText(this, "RECORDING STOPPED", Toast.LENGTH_SHORT).show();
                    //String msg = "Video capture succeeded: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                    Uri videoUri = ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                    sendToFirebase(videoUri);
                    showVideoTransferFragment();
                    //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                } else { //error while stopping the video
                    if(recording != null) {
                        recording.close();
                        recording = null;
                    }
                    String msg = "Error: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getError();
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
                //capture_video.setImageResource(R.drawable.camera_record); //change button image
                capture_video.setVisibility(View.VISIBLE);
            }
        });
    }

    //initializes and starts the camera based on the specified camera facing direction.
    private void startCamera(int cameraFacing) {
        //obtain an instance of ProcessCameraProvider, which manages the camera lifecycle.
        ListenableFuture<ProcessCameraProvider> processCameraProvider = ProcessCameraProvider.getInstance(RecordingActivity.this);
        Log.d("startCamera", "In startCamera");
        ((ListenableFuture<?>) processCameraProvider).addListener(() -> { //completion of obtaining the camera provider
            try {
                ProcessCameraProvider cameraProvider = processCameraProvider.get();
                //create a preview use case and set its surface provider
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(preview_view.getSurfaceProvider());

                //create a recorder use case with the specified quality setting
                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.LOWEST))
                        .build();
                video_capture = VideoCapture.withOutput(recorder);

                cameraProvider.unbindAll();
                //build the camera selector based on the specified facing direction
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing).build();

                //bind the camera with the specified use cases and lifecycle
                Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, video_capture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(RecordingActivity.this));
    }

    //send video to Firebase Storage with name "uid_date-and-time-of-recording.mp4" in Videos/New/ folder
    public void sendToFirebase(Uri uri) {
        double latitude = current_location.getLatitude();
        double longitude = current_location.getLongitude();

        // Get the current date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEEE");
        Date now = new Date();

        String date = dateFormat.format(now);
        String time = timeFormat.format(now);
        String dayOfWeek = dayOfWeekFormat.format(now);

        // Format the filename
        String filename = String.format("%s_%s_%s_%s_%s", latitude, longitude, date, dayOfWeek, time);
        filename = filename + ".mp4";

        Log.d("SLANJE VIDEA", filename + " iz sendtofb");

        StorageReference reference = FirebaseStorage.getInstance().getReference("Videos/" + filename);
        reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { //the video is successfully sent to Firebase and deleted from the device
                Toast.makeText(RecordingActivity.this, "Video poslan u bazu.", Toast.LENGTH_SHORT).show();
                //deleteVideoFromDevice(uri);
                videoTransferFragment.updateProgressText("Snimka uspješno poslana na obradu!");
            }
        }).addOnFailureListener(new OnFailureListener() { //the video failed to upload to the Firebase
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RecordingActivity.this, "Došlo je do greške: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                videoTransferFragment.updateProgressText("Nema pristupa internetu, snimka će biti automatski poslana kada veza bude uspostavljena.");
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() { //show progress of uploading the video to the database
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                videoTransferFragment.updateProgressText("Snimka se obrađuje, molim pričekajte...");
                Toast.makeText(RecordingActivity.this, "Progress: " + Math.toIntExact(snapshot.getBytesTransferred()) + "/" + Math.toIntExact(snapshot.getTotalByteCount()), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showVideoTransferFragment() {
        videoTransferFragment.show(getSupportFragmentManager(), "VideoTransferFragment");
    }


    @Override
    public void onVideoTransferProgress(String progress) {
        if (videoTransferFragment != null) {
            videoTransferFragment.updateProgressText(progress);
        }
    }

    // Call this method when you start recording
    private void startTimer() {
        // Start the recording logic here

        // Start the countdown timer
        timerCardView.setVisibility(View.VISIBLE);
        countDownTimer.start();
    }

    // Call this method when you stop recording
    private void stopTimer() {
        // Stop the recording logic here
        timerCardView.setVisibility(View.GONE);
        // Cancel the countdown timer if recording is stopped before the timer finishes
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
