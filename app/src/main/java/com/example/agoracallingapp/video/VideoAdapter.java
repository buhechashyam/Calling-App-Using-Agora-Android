package com.example.agoracallingapp.video;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agoracallingapp.R;

import java.util.ArrayList;
import java.util.Map;

import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.VideoCanvas;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    ArrayList<Integer> uids;
    Map<Integer, String> mParticipants;
    RtcEngine rtcEngine;
    Context context;

    public VideoAdapter(ArrayList<Integer> uids, Map<Integer, String> mParticipants, RtcEngine rtcEngine, Context context) {
        this.uids = uids;
        this.mParticipants = mParticipants;
        this.rtcEngine = rtcEngine;
        this.context = context;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        int uid = uids.get(position);
        SurfaceView remoteSurface = new SurfaceView(context);
        remoteSurface.setZOrderMediaOverlay(true);

        holder.container.addView(remoteSurface);
        rtcEngine.setupRemoteVideo(new VideoCanvas(remoteSurface, VideoCanvas.RENDER_MODE_FIT, uid));

        holder.mTextViewUserName.setText(mParticipants.get(uid));
    }

    @Override
    public int getItemCount() {
        return uids.size();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        FrameLayout container;
        TextView mTextViewUserName;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.frame_remote_user);
            mTextViewUserName = itemView.findViewById(R.id.text_username);
        }
    }
}
