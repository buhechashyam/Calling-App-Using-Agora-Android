package com.example.agoracallingapp.voice;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.agoracallingapp.R;
import com.example.agoracallingapp.databinding.ActivityConferenceAudioCallBinding;
import com.example.agoracallingapp.video.ConferenceVideoCallActivity;

import java.util.ArrayList;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;

public class ConferenceAudioCallActivity extends AppCompatActivity {

    private String appId = "4e7e978a48f04e15b507bd1f5bd96c56";
    private String mChannelName = "calling app";
    private String mToken = "007eJxTYDDbNidW2qvvU/OaGXt1+de5z/LjKHt1bck9BvV/t36J7rJWYDBJNU+1NLdINLFIMzBJNTRNMjUwT0oxTDNNSrE0SzY1e9ZZl9YQyMiQPH8fKyMDBIL4XAxl+ZnJqQrJiTk5DAwAU3Ui2Q==";
    RtcEngine rtcEngine;
    AudioAdapter adapter;
    int localUId = 0;
    private ArrayList<Integer> mListUIds = new ArrayList<>();
    ActivityConferenceAudioCallBinding binding;

    IRtcEngineEventHandler eventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showMessage("Join channel successfully");
                    binding.textStatus.setText("Join Channel Successfully");
                    localUId = uid;

                }
            });
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            super.onUserJoined(uid, elapsed);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (localUId!=uid){
                        mListUIds.add(uid);
                        adapter.notifyDataSetChanged();
                    }
                    showMessage("Join user" + uid);
                }
            });
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            super.onUserOffline(uid, reason);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mListUIds.remove(Integer.valueOf(uid));
                    adapter.notifyDataSetChanged();
                    binding.textStatus.setText("User is Offline");
                }
            });

        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityConferenceAudioCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (checkPermission()){
            initializeVoiceSDK();
            setUpRemoteUsers();
        }

        binding.btnJoinCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        joinAudioCall();
                    }
                });
            }
        });

        binding.btnEndCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        leaveAudioCall();
                    }
                });
            }
        });
    }

    private void leaveAudioCall() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rtcEngine.stopPreview();
                rtcEngine.leaveChannel();
                mListUIds.clear();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void joinAudioCall() {
        rtcEngine.enableAudio();
        rtcEngine.startPreview();

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
            if (ContextCompat.checkSelfPermission(ConferenceAudioCallActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
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
            setUpRemoteUsers();
        }
    }

    private void setUpRemoteUsers() {
        adapter = new AudioAdapter(mListUIds,ConferenceAudioCallActivity.this);
        binding.recyclerviewRemoteUsers.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        binding.recyclerviewRemoteUsers.setAdapter(adapter);
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
    private void showMessage(String msg) {
        runOnUiThread(() -> {
            Toast.makeText(ConferenceAudioCallActivity.this, msg, Toast.LENGTH_SHORT).show();
            Log.d("MAIN", msg);
        });
    }

}