package com.fadybasem.syriana;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.widget.MediaController;
import android.widget.Toast;

import java.io.File;

public class MediaControllerDelegate implements MediaController.MediaPlayerControl {
    MediaPlayer mediaPlayer;
    Context context;
    MediaRecorder recorder;


    public MediaControllerDelegate(MediaPlayer mediaPlayer, Context context, MediaRecorder recorder) {
        this.mediaPlayer = mediaPlayer;
        this.context = context;
        this.recorder = recorder;

        captureMic(() -> VideoActivity.playingAllowed = true, () -> {
            Toast.makeText(context, "Cannot play video during screen recording.", Toast.LENGTH_LONG).show();
            VideoActivity.playingAllowed = false;
        });

    }

    @Override
    public void start() {
        if (VideoActivity.playingAllowed) {
            mediaPlayer.start();
        } else {
            Toast.makeText(context, "Cannot play video during screen recording.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public int getCurrentPosition() {
        try {
            return mediaPlayer.getCurrentPosition();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return  0;
        }
    }

    @Override
    public void seekTo(int i) {
        mediaPlayer.seekTo(i);
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
    }

    private void captureMic (Runnable onSuccess, Runnable onFailure) {

        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            recorder.setOutputFile(File.createTempFile("tmp", ".mp4", context.getCacheDir()));
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

            recorder.prepare();
            recorder.start();

            VideoActivity.isRecording = true;
            onSuccess.run();
        } catch (Exception e) {
            onFailure.run();
        }

    }
}
