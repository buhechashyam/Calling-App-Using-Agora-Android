package com.example.agoracallingapp.voice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agoracallingapp.R;

import java.util.ArrayList;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioViewHolder> {

    ArrayList<Integer> mListUid;
    Context context;

    public AudioAdapter(ArrayList<Integer> mListUid, Context context) {
        this.mListUid = mListUid;
        this.context = context;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_audio,parent,false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        holder.mTextviewUser.setText("User : " + mListUid.get(position));
    }

    @Override
    public int getItemCount() {
        return mListUid.size();
    }

    public class AudioViewHolder extends RecyclerView.ViewHolder{
        TextView mTextviewUser;
        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextviewUser = itemView.findViewById(R.id.text_remote_user);
        }
    }

}
