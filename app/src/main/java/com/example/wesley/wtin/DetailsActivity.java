package com.example.wesley.wtin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class DetailsActivity extends AppCompatActivity {

    private TextView mDetailsNome, mOrientation, mBio;
    private String nome, profileImageUrl, orientation;
    private ImageView mProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            nome = extras.getString("nome");
            profileImageUrl = extras.getString("profileImageUrl");
            orientation = extras.getString("orientation");
        }


        mOrientation = findViewById(R.id.detailsOrientation);
        mDetailsNome = findViewById(R.id.detailsNome);
        mProfileImage = findViewById(R.id.profileImageDetails);

        mDetailsNome.setText(nome);
        mOrientation.setText(orientation);
        Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);

    }

}

