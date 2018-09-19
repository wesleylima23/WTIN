package com.example.wesley.wtin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigActivity extends AppCompatActivity {

    private EditText mName, mPhone;
    private Button mVoltar, mConfirm;
    private ImageView mProfileImage;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    private String userID, name, phone, profileImageUrl, interesse, orientation;

    private TextView mInteresse, mOrientation;

    private Uri resultUri;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        //String userSex;
        //userSex = getIntent().getExtras().getString("sex");

        //userSex = "male";
        mInteresse = findViewById(R.id.interesseConfigActivity);
        mOrientation = findViewById(R.id.orientationConfigActivity);



        mName = (EditText) findViewById(R.id.nome);
        mPhone = (EditText) findViewById(R.id.phone);

        mProfileImage = (ImageView) findViewById(R.id.profileImage);

        mConfirm = (Button) findViewById(R.id.confirmar);
        mVoltar = (Button) findViewById(R.id.voltar);
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
      //mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        getUserInfo();
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });
        mVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void getUserInfo() {
        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        name = map.get("name").toString();
                        mName.setText(name);
                    }
                    if(map.get("phone")!=null){
                        phone = map.get("phone").toString();
                        mPhone.setText(phone);
                    }
                    if(map.get("interesse")!=null){
                        interesse = map.get("interesse").toString();
                        mInteresse.setText(interesse);
                    }
                    if(map.get("orientation")!=null){
                        orientation = map.get("orientation").toString();
                        mOrientation.setText(orientation);
                    }
                    if(map.get("profileImageUrl")!=null){
                        profileImageUrl = map.get("profileImageUrl").toString();
                        switch (profileImageUrl){

                            case "default":
                                String temp = "https://firebasestorage.googleapis.com/v0/b/wtin-cfc43.appspot.com/o/imagem_1.jpg?alt=media&token=f31e499e-7aaa-4d63-8449-359a4171f4ae";
                                Glide.with(getApplication()).load(R.mipmap.ic_launcher).into(mProfileImage);
                                //Glide.with(getContext()).load(R.mipmap.ic_launcher).into(image);
                                //Glide.with(getContext()).load(R.mipmap.ic_launcher).into(mProfileImage);
                                break;

                            default:
                                Log.e("ERRO","Imagem nao default devia ter aparecido");
                                Log.e("Erro","Path da foto "+ profileImageUrl);
                                Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                        }
                        //Glide.with(getApplication()).load(profileImageUrl).into(mProfileImage);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveUserInformation() {
        name = mName.getText().toString();
        phone = mPhone.getText().toString();
        interesse = mInteresse.getText().toString();
        orientation = mOrientation.getText().toString();
        Map userInfo  = new HashMap<>();
        userInfo.put("name", name);
        userInfo.put("phone",phone);
        userInfo.put("interesse",interesse);
        userInfo.put("orientation",orientation);
        mUserDatabase.updateChildren(userInfo);
        if(resultUri != null){
            StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImage").child(userID);
            Bitmap bitmap = null;
            //Bitmap resized = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri);
                //bitmap = Bitmap.createScaledBitmap(bitmap,500, 500, true);

            } catch (IOException e) {
                //Log.e("erro",resized.getHeight()+"");
                e.printStackTrace();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = filepath.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //finish();
                    e.printStackTrace();
                    Log.e("error","falhou upload da foto");
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                   //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                   Uri downloadUrl = taskSnapshot.getDownloadUrl();
                   Map userInfo  = new HashMap<>();
                   userInfo.put("profileImageUrl", downloadUrl.toString());
                   mUserDatabase.updateChildren(userInfo);
                   finish();
                }
            });

        }else{
            Toast.makeText(getApplicationContext(), "Imagem Obrigatoria!!!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            resultUri = data.getData();
            mProfileImage.setImageURI(resultUri);
        }
    }
}
