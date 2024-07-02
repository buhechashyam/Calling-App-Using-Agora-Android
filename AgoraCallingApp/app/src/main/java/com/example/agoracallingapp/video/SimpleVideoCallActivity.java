package com.example.agoracallingapp.video;

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

import com.example.agoracallingapp.R;
import com.example.agoracallingapp.databinding.ActivitySimpleVideoCallBinding;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class SimpleVideoCallActivity extends AppCompatActivity {

    ActivitySimpleVideoCallBinding binding;
    RtcEngine rtcEngine;
    SurfaceView remoteSurface;
    SurfaceView localSurface;
    int uid = 2; // this local uid, it;s unique for each user. different device different uid
    private String appId = "4e7e978a48f04e15b507bd1f5bd96c56";
    private String token = "007eJxTYGg1MUxnyRB/4ut+du7yK9vXbbS9oT/vzwnRTH1u3jL97t8KDCap5qmW5haJJhZpBiaphqZJpgbmSSmGaaZJKZZmyaZm3y82pzUEMjJMV01nZmSAQBCfmyE5MScnMy9dIbGggIEBAALnIbU=";
    private String channelName = "calling app";
    boolean isJoined = false;
    int localuid = 0;


    private final IRtcEngineEventHandler eventHandler = new IRtcEngineEventHandler() {
        int remoteUId = 0;
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            localuid = uid;
            showToast("Join Channel Success " + uid);
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            if(remoteUId == 0) {
                remoteUId = uid;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("User join : " + uid);
                        binding.textJoinUser.setText(uid + " is Joined");
                        setUpRemoteVideo(remoteUId);
                    }
                });
            }else {
                showToast("it is one to one call");
            }

        }

        @Override
        public void onUserOffline(int uid, int reason){
            showToast("User Offline : " + uid);
            if (remoteUId == uid){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rtcEngine.stopPreview();
                        rtcEngine.leaveChannel();
                        localSurface.setVisibility(View.GONE);
                        remoteSurface.setVisibility(View.GONE);
                        binding.textJoinUser.setText("Call Ended");
                        remoteUId = 0;
                    }
                });

            }


        }
    };

    private void setUpRemoteVideo(int uid) {

        FrameLayout container = findViewById(R.id.fragment_remote);
        remoteSurface = new SurfaceView(getBaseContext());
        remoteSurface.setZOrderMediaOverlay(true);
        container.addView(remoteSurface);

        rtcEngine.setupRemoteVideo(new VideoCanvas(remoteSurface, VideoCanvas.RENDER_MODE_FIT, uid));

    }

    private void setUpLocalVideo() {
        FrameLayout container = findViewById(R.id.fragment_local);
        localSurface = new SurfaceView(getBaseContext());
        container.addView(localSurface);
        rtcEngine.setupLocalVideo(new VideoCanvas(localSurface, VideoCanvas.RENDER_MODE_FIT, 0));
    }

    private void setUpVideoSDK() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mContext = getBaseContext();
            config.mAppId = appId;
            config.mEventHandler = eventHandler;
            rtcEngine = RtcEngine.create(config);
        } catch (Exception e) {
            throw new RuntimeException("Check the error.");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySimpleVideoCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (checkPermission()) {
            //Setup Video Sdk
            setUpVideoSDK();
        } else {
            ActivityCompat.requestPermissions(this, getAllPermissions(), 100);
        }

        binding.btnJoin.setOnClickListener(v -> joinChannel());

        binding.btnLeave.setOnClickListener(v -> {

            leaveCall();

        });

    }

    private void leaveCall() {
        rtcEngine.stopPreview();
        rtcEngine.leaveChannel();
        localSurface.setVisibility(View.GONE);
        remoteSurface.setVisibility(View.GONE);
        binding.textJoinUser.setText("Call Ended");
    }

    private void joinChannel() {

        rtcEngine.enableVideo();
        rtcEngine.startPreview();

        setUpLocalVideo();

        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
        rtcEngine.joinChannel(token, channelName, 0, options);

    }


    private String[] getAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.READ_PHONE_STATE
            };
        } else {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            };
        }
    }

    private boolean checkPermission() {
        for (String permission : getAllPermissions()) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (checkPermission()) {
            setUpVideoSDK();
        }
    }

    private void showToast(String msg) {
        runOnUiThread(() -> {
            Toast.makeText(SimpleVideoCallActivity.this, msg, Toast.LENGTH_SHORT).show();
            Log.d("MAIN", msg);
        });
    }
}