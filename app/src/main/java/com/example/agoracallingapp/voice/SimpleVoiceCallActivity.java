package com.example.agoracallingapp.voice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.agoracallingapp.databinding.ActivitySimpleVoiceCallBinding;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;

public class SimpleVoiceCallActivity extends AppCompatActivity {
    ActivitySimpleVoiceCallBinding binding;
    private String appId = "4e7e978a48f04e15b507bd1f5bd96c56";
    private String mChannelName = "calling app";
    private String mToken = "007eJxTYJAr5XRor+V5HdGsvfnP9AW7ixbOLZLwPXN3LgMbs3vhn40KDCap5qmW5haJJhZpBiaphqZJpgbmSSmGaaZJKZZmyaZm3Uuq0xoCGRk+fJnAwAiFID43Q3JiTk5mXrpCYkEBAwMAAYIiPg==";

    private RtcEngine rtcEngine;
    private final IRtcEngineEventHandler rtcEngineEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);

            showToast("Join Channel Success");
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);
            showToast("User join : " + uid);
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            super.onUserOffline(uid, reason);
            showToast("User Offline : " + uid);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySimpleVoiceCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (checkPermission()){
            initializeAndJoinChannel();
        }else {
            ActivityCompat.requestPermissions(SimpleVoiceCallActivity.this,getRequirePermissions(),100);
        }
    }

    private void initializeAndJoinChannel(){

        //Create RtcEngineConfig Object
        RtcEngineConfig rtcEngineConfig = new RtcEngineConfig();
        rtcEngineConfig.mAppId = appId;
        rtcEngineConfig.mEventHandler = rtcEngineEventHandler;
        rtcEngineConfig.mContext = getBaseContext();

        try {
            //Initialize RtcEngine object
            rtcEngine = RtcEngine.create(rtcEngineConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;

        rtcEngine.joinChannel(mToken,mChannelName,0,options);


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
            initializeAndJoinChannel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rtcEngine.leaveChannel();
    }

    private void showToast(String msg){
        runOnUiThread(() -> {
            Toast.makeText(SimpleVoiceCallActivity.this, msg, Toast.LENGTH_SHORT).show();
            Log.d("MAIN",msg);
        });
    }
}