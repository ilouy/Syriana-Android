package com.fadybasem.syriana;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import android.Manifest;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Amplify;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public class VideoActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener, EasyPermissions.PermissionCallbacks {

    String instructor;
    String course;
    String video;

    SurfaceView surfaceView;
    MediaPlayer mediaPlayer;
    MediaController mediaController;

    ImageView playbackSpeedImageView;

    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.0f;
    private boolean isScaling = false;

    private GestureDetector panGestureDetector;

    final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 132;

    MediaRecorder recorder = new MediaRecorder();

    public static boolean isRecording = false;
    public static boolean playingAllowed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        instructor = getIntent().getStringExtra("instructor");
        course = getIntent().getStringExtra("course");
        video = getIntent().getStringExtra("video");

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.setPivotX(0);

        playbackSpeedImageView = findViewById(R.id.playbackSpeedImageView);

        playbackSpeedImageView.setOnClickListener(view -> {
            createPlaybackSpeedDialog().show();
        });

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        SyrianaApp.getAccessToken(() -> loadVideo(instructor, course, video, SyrianaApp.accessToken));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        panGestureDetector = new GestureDetector(this, new PanListener());

    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sp = getSharedPreferences("VideoPlayback", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putInt(instructor+course+video, mediaPlayer.getCurrentPosition());
        ed.apply();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mediaPlayer.release();
        mediaController.hide();

        try {
            if (isRecording) {
                recorder.stop();
                recorder.release();

                isRecording = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void loadVideo (String instructor, String course, String video, String accessToken) {
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("instructor", instructor);
        queryParams.put("course", course);
        queryParams.put("video", video);

        RestOptions options = RestOptions.builder()
                .addPath("/video")
                .addQueryParameters(queryParams)
                .addHeader("Authorization", accessToken)
                .build();

        Amplify.API.get(options,
                restResponse -> {

                    try {
                        if (restResponse.getCode().isSuccessful()) {
                            JSONObject json = (JSONObject) new JSONParser().parse(restResponse.getData().asString());

                            String videoURL = (String) json.get("url");
                            System.out.println(videoURL);

                            runOnUiThread(() -> {
                                setupMediaPlayer(videoURL);

                                mediaController = new MediaController(VideoActivity.this);
                                mediaController.setAnchorView(findViewById(R.id.videoConstraintLayout));
                                mediaController.setMediaPlayer(new MediaControllerDelegate(mediaPlayer, this, recorder));

                                mediaController.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                                    @Override
                                    public void onViewAttachedToWindow(@NonNull View view) {
                                        playbackSpeedImageView.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onViewDetachedFromWindow(@NonNull View view) {
                                        playbackSpeedImageView.setVisibility(View.GONE);
                                    }
                                });

                            });
                        } else {
                            //TODO: handle unauthorized access
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                },
                apiFailure -> Log.e("MyAmplifyApp", "GET failed.", apiFailure)
        );
    }

    private Dialog createPlaybackSpeedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        CharSequence [] speeds = {"0.5x", "0.75x", "1.0x", "1.25x", "1.5x", "1.75x", "2.0x"};

        builder.setTitle("Playback Speed")
                .setSingleChoiceItems(speeds, 2,
                        (dialogInterface, i) -> {
                            PlaybackParams myPlayBackParams = new PlaybackParams();

                            switch (i) {
                                case 0:
                                    myPlayBackParams.setSpeed(0.5f);
                                    break;
                                case 1:
                                    myPlayBackParams.setSpeed(0.75f);
                                    break;
                                case 2:
                                    myPlayBackParams.setSpeed(1.0f);
                                    break;
                                case 3:
                                    myPlayBackParams.setSpeed(1.25f);
                                    break;
                                case 4:
                                    myPlayBackParams.setSpeed(1.5f);
                                    break;
                                case 5:
                                    myPlayBackParams.setSpeed(1.75f);
                                    break;
                                case 6:
                                    myPlayBackParams.setSpeed(2.0f);
                                    break;
                            }
                            mediaPlayer.setPlaybackParams(myPlayBackParams);
                        })
                .setNeutralButton("Done", (dialogInterface, i) -> {

                });

        return builder.create();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        panGestureDetector.onTouchEvent(event);
        mediaController.show();

        return true;
    }

    private void setupMediaPlayer (String videoURL) {
        try {
            Uri uri = Uri.parse(videoURL);

            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(this, uri);

            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    //.setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                    .setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_NONE)
                    .build());

            mediaPlayer.setSurface(surfaceView.getHolder().getSurface());
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);

            mediaPlayer.prepareAsync();
            this.mediaPlayer = mediaPlayer;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        SharedPreferences sp = this.getSharedPreferences("VideoPlayback", MODE_PRIVATE);
        mediaPlayer.seekTo(sp.getInt(instructor+course+video, 0));

        mediaPlayer.getAudioSessionId();

        if (!EasyPermissions.hasPermissions(this, Manifest.permission.RECORD_AUDIO)) {
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, RECORD_AUDIO_PERMISSION_REQUEST_CODE, Manifest.permission.RECORD_AUDIO)
                            .setRationale(R.string.record_audio_permission_rationale)
                            .setPositiveButtonText(android.R.string.ok)
                            .setNegativeButtonText(android.R.string.cancel)
                            .build());

            mediaController.setEnabled(false);
        } else {
            mediaController.setEnabled(true);
        }

    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int videoWidth, int videoHeight) {
        if(mediaPlayer != null)
        {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

            int screenWidth = displaymetrics.widthPixels;
            int screenHeight = displaymetrics.heightPixels;
            android.view.ViewGroup.LayoutParams videoParams = surfaceView.getLayoutParams();

            if (videoWidth > videoHeight)
            {
                videoParams.width = screenWidth;
                videoParams.height = screenWidth * videoHeight / videoWidth;
            }
            else
            {
                videoParams.width = screenHeight * videoWidth / videoHeight;
                videoParams.height = screenHeight;
            }


            surfaceView.setLayoutParams(videoParams);
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
            isScaling = true;
            return true;
        }

        @Override
        public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
            isScaling = false;
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            scaleFactor *= scaleGestureDetector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f));
            surfaceView.setScaleX(scaleFactor);
            surfaceView.setScaleY(scaleFactor);

            return true;
        }

    }

    private class PanListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

            if (!isScaling && scaleFactor != 1) {
                if (Math.abs(e2.getX() - e1.getX()) > Math.abs(e2.getY() - e1.getY()))
                    surfaceView.setTranslationX(surfaceView.getTranslationX() - distanceX);
                else
                    surfaceView.setTranslationY(surfaceView.getTranslationY() - distanceY);

            }
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "Enabled", Toast.LENGTH_LONG).show();
        mediaController.setEnabled(true);
        mediaPlayer.start();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        mediaController.setEnabled(false);
        mediaPlayer.stop();

        Toast.makeText(this, "Please allow audio recording permission to play this video.", Toast.LENGTH_LONG).show();
    }
}