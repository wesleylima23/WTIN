package com.example.wesley.wtin.Matches;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.wesley.wtin.Chat.ChatActivity;
import com.example.wesley.wtin.R;

public class MatchViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView mMatchId,mMatchName;
    public ImageView mImageView;
    public LinearLayout mLinearLayout;
    public MatchViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        mMatchId = (TextView) itemView.findViewById(R.id.matchId);
        mMatchName = (TextView) itemView.findViewById(R.id.matchName);
        mImageView = (ImageView) itemView.findViewById(R.id.matchImageView);
        //mLinearLayout = (LinearLayout) itemView.findViewById(R.id.layoutBoxItemMatch);
        //mLinearLayout.setBackgroundColor(Color.RED);
    }

    @Override
    public void onClick(View view) {
        Intent intent  = new Intent(view.getContext(), ChatActivity.class);
        Bundle b =  new Bundle();
        b.putString("matchId", mMatchId.getText().toString());
        intent.putExtras(b);
        view.getContext().startActivity(intent);
    }
}
