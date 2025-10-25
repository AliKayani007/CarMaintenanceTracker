package com.example.carmaintenance;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private VideoView videoView;
    private static final int SPLASH_DURATION = 6000; // 6 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize VideoView
        videoView = findViewById(R.id.videoView);

        // Set up video
        setupVideo();

        // Set up timer to navigate to MainActivity after 6 seconds
        setupTimer();
    }

    private void setupVideo() {
        try {
            // Get video URI from raw resources
            Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.logosplashscreen);
            
            // Set video URI
            videoView.setVideoURI(videoUri);
            
            // Set up video completion listener
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    // Video completed, navigate to MainActivity
                    navigateToMainActivity();
                }
            });

            // Set up error listener
            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                    // If video fails to play, still navigate after timer
                    return false;
                }
            });

            // Start playing the video
            videoView.start();

        } catch (Exception e) {
            e.printStackTrace();
            // If video setup fails, navigate after timer
        }
    }

    private void setupTimer() {
        // Create a handler to navigate to MainActivity after 6 seconds
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToMainActivity();
            }
        }, SPLASH_DURATION);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Finish splash activity so user can't go back to it
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause video when activity is paused
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume video when activity is resumed
        if (videoView != null) {
            videoView.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up video resources
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}
