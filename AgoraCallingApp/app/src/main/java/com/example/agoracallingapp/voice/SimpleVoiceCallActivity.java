package com.example.agoracallingapp.voice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.agoracallingapp.R;
import com.example.agoracallingapp.databinding.ActivitySimpleVoiceCallBinding;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class SimpleVoiceCallActivity extends AppCompatActivity {
    ActivitySimpleVoiceCallBinding binding;
    private String appId = "4e7e978a48f04e15b507bd1f5bd96c56";
    private String mChannelName = "calling app";
    private String mToken = "007eJxTYDDbNidW2qvvU/OaGXt1+de5z/LjKHt1bck9BvV/t36J7rJWYDBJNU+1NLdINLFIMzBJNTRNMjUwT0oxTDNNSrE0SzY1e9ZZl9YQyMiQPH8fKyMDBIL4XAxl+ZnJqQrJiTk5DAwAU3Ui2Q==";
    private int localUid = 0;
    private boolean isJoined = false;
    private RtcEngine rtcEngine;

    IRtcEngineEventHandler eventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showToast("Join channel successfully");
                    binding.textStatus.setText("Join Channel Successfully");
                    localUid = uid;
                    isJoined = true;
                }
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    userJoin(uid);
                    showToast("Join user" + uid);
                }
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            super.onUserOffline(uid, reason);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isJoined = false;
                    binding.textStatus.setText("User is Offline");
                }
            });
            showToast("User Offline" + uid);
        }

    };

    private void userJoin(int uid) {
        binding.textStatus.setText(localUid + " (ME) Connect to " + uid );

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySimpleVoiceCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (checkPermission()) {
            initializeVoiceSDK();
        } else {
            ActivityCompat.requestPermissions(SimpleVoiceCallActivity.this, getRequirePermissions(), 100);
        }

        binding.btnJoinCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        joinVoiceCall();
                    }
                });

            }
        });

        binding.btnEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rtcEngine.stopPreview();
                        rtcEngine.leaveChannel();
                        binding.textStatus.setText("Call Ended");
                    }
                });
            }
        });
    }

    private void joinVoiceCall() {

        rtcEngine.enableAudio();
        rtcEngine.startPreview();

        ChannelMediaOptions options = new ChannelMediaOptions();
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;

        rtcEngine.joinChannel(mToken, mChannelName, 0, options);
    }

    private void initializeVoiceSDK() {

        RtcEngineConfig config = new RtcEngineConfig();
        config.mAppId = appId;
        config.mContext = getBaseContext();
        config.mEventHandler = eventHandler;

        try {
            rtcEngine = RtcEngine.create(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getRequirePermissions() {
        //Permission for version 31 or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE, Manifest.permission.BLUETOOTH_CONNECT};
        } else {
            return new String[]{Manifest.permission.RECORD_AUDIO};
        }
    }

    private boolean checkPermission() {
        for (String permission : getRequirePermissions()) {
            if (ContextCompat.checkSelfPermission(SimpleVoiceCallActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (checkPermission()) {
            initializeVoiceSDK();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rtcEngine.leaveChannel();
    }

    private void showToast(String msg) {
        runOnUiThread(() -> {
            Toast.makeText(SimpleVoiceCallActivity.this, msg, Toast.LENGTH_SHORT).show();
            Log.d("MAIN", msg);
        });
    }
}