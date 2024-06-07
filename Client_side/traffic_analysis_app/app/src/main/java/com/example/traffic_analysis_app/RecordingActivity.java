package com.example.traffic_analysis_app;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class RecordingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);
    }
}
/*
package com.example.exerciseproject;

import static android.os.Build.*;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import android.Manifest;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/*public class MainActivity extends AppCompatActivity {
    TextView elapsed_time;
    ImageView capture_video;
    Recording recording = null;
    VideoCapture<Recorder> video_capture = null;
    PreviewView preview_view;
    BottomNavigationView bottom_navigation;
    Toolbar toolbar;
    private VideoTransferFragment videoTransferFragment = VideoTransferFragment.newInstance();
    int camera_facing = CameraSelector.LENS_FACING_BACK;
    private final ActivityResultLauncher<String> activity_result_launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            startCamera(camera_facing);
        }
    });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bottom_navigation_bar, menu);
        return true;
    }

    //defining the action that is done after navigation bar item selection
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.analysis) {
            Intent analysis = new Intent(getApplicationContext(), AnalysisActivity.class);
            startActivity(analysis);
            finish();
        } else if(id == R.id.data) {
            //userInstructions(this);
            Intent data = new Intent(getApplicationContext(), DataScreen.class);
            startActivity(data);
            finish();
        } else if(id == R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else if(id == R.id.info){
            Intent info = new Intent(getApplicationContext(), InfoScreen.class);
            startActivity(info);
            finish();
        } else if(id == R.id.settings){
            Intent settings = new Intent(getApplicationContext(), UserSettings.class);
            startActivity(settings);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showWarningDialog();

        //get variable ids from resources
        capture_video = findViewById(R.id.video_capture);
        preview_view = findViewById(R.id.camera_preview);
        elapsed_time = findViewById(R.id.elapsed_time);
        elapsed_time.setText("00:00:00");
        bottom_navigation = findViewById(R.id.bottom_navigation);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Toast.makeText(MainActivity.this, "Za slanje videa u bazu podataka potrebna je povezanost s internetom.", Toast.LENGTH_LONG).show();


        bottom_navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if(id == R.id.analysis){
                    Intent analysis = new Intent(getApplicationContext(), AnalysisActivity.class);
                    startActivity(analysis);
                    finish();
                } else if(id == R.id.data) {
                    Intent data = new Intent(getApplicationContext(), DataScreen.class);
                    startActivity(data);
                    finish();
                } else if(id == R.id.info){
                    Intent info = new Intent(getApplicationContext(), InfoScreen.class);
                    startActivity(info);
                    finish();
                }
                return false;
            }
        });

        //handle click on recording button - stop or start recording
        capture_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if permissions for video recording not already given, ask for them
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    activity_result_launcher.launch(android.Manifest.permission.CAMERA);
                } else if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
                    activity_result_launcher.launch(android.Manifest.permission.RECORD_AUDIO);
                } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    activity_result_launcher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                } else {
                    captureVideo();
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activity_result_launcher.launch(android.Manifest.permission.CAMERA);
        } else {
            startCamera(camera_facing);
        }

    }

    private void showWarningDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        RecordWarningFragment warning_popup = RecordWarningFragment.newInstance();
        warning_popup.show(fragmentManager, "warning_dialog");
        videoTransferFragment = VideoTransferFragment.newInstance();
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

    //method to handle capturing video recording
    private void captureVideo() {
        //capture_video.setImageResource(R.drawable.baseline_stop_24); //change buttom image
        Recording recording1 = recording;
        Log.d("Delete video", "captureVideo recording = " + String.valueOf(recording1));
        if (recording1 != null) { //stop the recording
            recording1.stop();
            //sendToFirebase(recording);
            recording = null;
            return;
        }
        //save video recording to the device - name format is uid_year_month_day_hour_minute_second_millisecond.mp4
        String name = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
        name = FirebaseAuth.getInstance().getUid() + "_" + name;
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/ExerciseProject");

        MediaStoreOutputOptions options = new MediaStoreOutputOptions.Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues).build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //get type of action - starting or stopping the recording of the video
        recording = video_capture.getOutput().prepareRecording(MainActivity.this, options).withAudioEnabled().start(ContextCompat.getMainExecutor(MainActivity.this), videoRecordEvent -> {
            if (videoRecordEvent instanceof VideoRecordEvent.Start) {
                Toast.makeText(this, "RECORDING STARTED", Toast.LENGTH_SHORT).show();
                capture_video.setEnabled(true); //start recording
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
                //capture_video.setImageResource(R.drawable.baseline_fiber_manual_record_24); //change button image
            }
        });
    }

    //find out file name from uri
    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        Cursor cursor = null;
        try {
            //iterate through media until you fin the file
            String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
            cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                fileName = cursor.getString(columnIndex);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return fileName;
    }

    //deleting the video from the device storage
    private void deleteVideoFromDevice(Uri videoUri) {
        try {
            // Get the content resolver
            ContentResolver contentResolver = getContentResolver();

            // Delete the video using the content resolver
            int rowsDeleted = contentResolver.delete(videoUri, null, null);

            // Check if the deletion was successful
            if (rowsDeleted > 0) {
                Log.d("Delete video", "Video deleted successfully");
                // Video deleted successfully
                //Toast.makeText(this, "Video deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Failed to delete the video
                Toast.makeText(this, "Failed to delete the video", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // Handle exceptions, e.g., SecurityException
            e.printStackTrace();
            Toast.makeText(this, "Error deleting video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    //send video to Firebase Storage with name "uid_date-and-time-of-recording.mp4" in Videos/New/ folder
    public void sendToFirebase(Uri uri) {
        String uid = FirebaseAuth.getInstance().getUid();
        //String filename = uid + "_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
        //String filename = new File(uri.getPath()).getName();
        String filename = getFileNameFromUri(uri);

        Log.d("SLANJE VIDEA", filename + " iz sendtofb");

        StorageReference reference = FirebaseStorage.getInstance().getReference("Videos/New/" + filename);
        reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { //the video is successfully sent to Firebase and deleted from the device
                //Toast.makeText(MainActivity.this, "Video poslan u bazu.", Toast.LENGTH_SHORT).show();
                deleteVideoFromDevice(uri);
                DatabaseReference database_reference = FirebaseDatabase.getInstance().getReference();
                database_reference.child("Unanalyzed").setValue(true);
                videoTransferFragment.updateProgressText("Snimka uspješno poslana na obradu!");
            }
        }).addOnFailureListener(new OnFailureListener() { //the video failed to upload to the Firebase
            @Override
            public void onFailure(@NonNull Exception e) {
                //Toast.makeText(MainActivity.this, "Došlo je do greške: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                videoTransferFragment.updateProgressText("Nema pristupa internetu, snimka će biti automatski poslana kada veza bude uspostavljena.");
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() { //show progress of uploading the video to the database
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                //Toast.makeText(MainActivity.this, "Progress: " + Math.toIntExact(snapshot.getBytesTransferred()) + "/" + Math.toIntExact(snapshot.getTotalByteCount()), Toast.LENGTH_SHORT).show();
                videoTransferFragment.updateProgressText("Snimka se obrađuje, molim pričekajte...");
            }
        });
    }

    //initializes and starts the camera based on the specified camera facing direction.
    private void startCamera(int cameraFacing) {
        //obtain an instance of ProcessCameraProvider, which manages the camera lifecycle.
        ListenableFuture<ProcessCameraProvider> processCameraProvider = ProcessCameraProvider.getInstance(MainActivity.this);
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
        }, ContextCompat.getMainExecutor(MainActivity.this));
    }
}
 */