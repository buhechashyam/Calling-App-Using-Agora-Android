package com.example.agoracallingapp.video;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.agoracallingapp.databinding.ActivityConferenceVideoCallBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.video.VideoCanvas;

public class ConferenceVideoCallActivity extends AppCompatActivity {

    ActivityConferenceVideoCallBinding binding;
    private RtcEngine rtcEngine;
    private String appID = "4e7e978a48f04e15b507bd1f5bd96c56";
    private String channelName = "calling app";
    private String token = "007eJxTYDDbNidW2qvvU/OaGXt1+de5z/LjKHt1bck9BvV/t36J7rJWYDBJNU+1NLdINLFIMzBJNTRNMjUwT0oxTDNNSrE0SzY1e9ZZl9YQyMiQPH8fKyMDBIL4XAxl+ZnJqQrJiTk5DAwAU3Ui2Q==";
    private ArrayList<Integer> uids = new ArrayList<>(); // all channel participant uid
    private Map<Integer, String> mParticipants = new HashMap<>();
    private int localUId = -1; // local user uid
    private String hostName = "Shyam";
    private boolean isJoined = false;
    VideoAdapter videoAdapter;


    private final IRtcEngineEventHandler handler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            //when user join channel successfully. user have assign uid. which is local uid
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    localUId = uid;
                    mParticipants.put(uid, hostName);
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
                    if (uid != localUId) {
                        uids.add(uid);
                        String participantName = "Participant-" + uid;
                        mParticipants.put(uid, participantName);
                        videoAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    uids.remove(Integer.valueOf(uid));
                    mParticipants.remove(uid);
                    videoAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityConferenceVideoCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (checkPermissions()) {
            setUpVideoSdk();
            setUpRemoteUser();
        } else {
            ActivityCompat.requestPermissions(ConferenceVideoCallActivity.this, getAllPermissions(), 100);
        }
        //Join a call
        binding.btnJoinCall.setOnClickListener(v -> {
            binding.btnJoinCall.setEnabled(false);
            joinCall();
        });
        //Leave a call
        binding.btnLeaveCall.setOnClickListener(v -> {
            leaveCall();
        });
        //Switch camara
        binding.btnSwitchCamera.setOnClickListener(v -> switchCamara());


    }

    private void setUpRemoteUser() {
        binding.rvRemoteUsers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        videoAdapter = new VideoAdapter(uids, mParticipants, rtcEngine, ConferenceVideoCallActivity.this);
        binding.rvRemoteUsers.setAdapter(videoAdapter);
    }

    private void switchCamara() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rtcEngine.switchCamera();
                showMessage("Switch a Camara"); 
            }
        });
    }

    private void leaveCall() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rtcEngine.stopPreview();
                rtcEngine.leaveChannel();

                binding.frameLocalUser.removeAllViews();
                binding.imgLocalUser.setVisibility(View.VISIBLE);
                binding.frameLocalUser.setVisibility(View.GONE);

                isJoined = false;
                uids.clear();
                mParticipants.clear();
                videoAdapter.notifyDataSetChanged();
                showMessage("Leave a call");

            }
        });
    }

    private void joinCall() {
        rtcEngine.enableVideo();
        rtcEngine.startPreview();

        setUpLocalVideo();

        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;

        rtcEngine.joinChannel(token, channelName, 0, options);
        showMessage("Join call");

    }

    private void setUpLocalVideo() {

        binding.frameLocalUser.setVisibility(View.VISIBLE);
        binding.imgLocalUser.setVisibility(View.GONE);

        SurfaceView localSurface = new SurfaceView(ConferenceVideoCallActivity.this);
        localSurface.setZOrderMediaOverlay(true);
        binding.frameLocalUser.addView(localSurface);

        rtcEngine.setupLocalVideo(new VideoCanvas(localSurface, VideoCanvas.RENDER_MODE_HIDDEN, 0));
    }

    private void setUpVideoSdk() {
        try {
            RtcEngineConfig config = new RtcEngineConfig();
            config.mAppId = appID;
            config.mContext = getBaseContext();
            config.mEventHandler = handler;

            //initialize RtcEngine
            rtcEngine = RtcEngine.create(config);
        } catch (Exception e) {
            showMessage("Error : " + e);
        }
    }

    private boolean checkPermissions() {
        for (String permission : getAllPermissions()) {
            if (ActivityCompat.checkSelfPermission(ConferenceVideoCallActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    private String[] getAllPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            return new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA
            };
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (checkPermissions()) {
            setUpVideoSdk();
        }
    }

    private void showMessage(String msg) {
        runOnUiThread(() -> {
            Toast.makeText(ConferenceVideoCallActivity.this, msg, Toast.LENGTH_SHORT).show();
            Log.d("MAIN", msg);
        });
    }
}