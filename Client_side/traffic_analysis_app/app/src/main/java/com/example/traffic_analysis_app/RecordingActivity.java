package com.example.traffic_analysis_app;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.concurrent.ExecutionException;

public class RecordingActivity extends AppCompatActivity {

    Button capture_video;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        capture_video = findViewById(R.id.video_capture);
        preview_view = findViewById(R.id.camera_preview);

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
        /*String name = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
        name = FirebaseAuth.getInstance().getUid() + "_" + name;*/
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
            } else if (videoRecordEvent instanceof VideoRecordEvent.Finalize) { //stop recording
                if (!((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                    Toast.makeText(this, "RECORDING STOPPED", Toast.LENGTH_SHORT).show();
                    //String msg = "Video capture succeeded: " + ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                    Uri videoUri = ((VideoRecordEvent.Finalize) videoRecordEvent).getOutputResults().getOutputUri();
                    sendToFirebase(videoUri);
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
        //String uid = FirebaseAuth.getInstance().getUid();
        //String filename = uid + "_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.getDefault()).format(System.currentTimeMillis());
        //String filename = new File(uri.getPath()).getName();
        //String filename = getFileNameFromUri(uri);
        String filename = "demo";

        Log.d("SLANJE VIDEA", filename + " iz sendtofb");

        StorageReference reference = FirebaseStorage.getInstance().getReference("Videos/" + filename);
        reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { //the video is successfully sent to Firebase and deleted from the device
                Toast.makeText(RecordingActivity.this, "Video poslan u bazu.", Toast.LENGTH_SHORT).show();
                //deleteVideoFromDevice(uri);
                DatabaseReference database_reference = FirebaseDatabase.getInstance().getReference();
                database_reference.child("Unanalyzed").setValue(true);
            }
        }).addOnFailureListener(new OnFailureListener() { //the video failed to upload to the Firebase
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RecordingActivity.this, "Došlo je do greške: "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() { //show progress of uploading the video to the database
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                Toast.makeText(RecordingActivity.this, "Progress: " + Math.toIntExact(snapshot.getBytesTransferred()) + "/" + Math.toIntExact(snapshot.getTotalByteCount()), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
