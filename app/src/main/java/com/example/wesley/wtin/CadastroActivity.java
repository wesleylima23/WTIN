package com.example.wesley.wtin;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.wesley.wtin.Alert.Alert;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CadastroActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Button mBotaoCadastro;
    private TextInputEditText mEmail, mSenha, mNome, mPhone;

    private RadioGroup mRadioGroupSexo,mRadioGroupOrientacao, mRadioGroupInteresse, mRadioGroupTopics;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthStateListener;

    private DatabaseReference currentUserDb;
    private ImageView mProfileImage;

    private Uri resultUri;

    private static final String FAILED = "NAO";
    private String email,senha, distancia,dsts;
    private String latitude = "false" ,longitude = "false";

    private LocationManager locationManager = null;
    private LocationListener listener = null;
    private Spinner mSpinnerProximidade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            email = extras.getString("email");
            senha = extras.getString("senha");
        }


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);



        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude() + "";
                longitude = location.getLongitude() + "";
                String distancia = getDistance(location.getLatitude(),
                        location.getLongitude(),
                        Double.parseDouble("-3.096916"),
                        Double.parseDouble("-60.068232")
                );
                Log.e("distancia",Double.parseDouble(distancia) + "m");
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };





        mAuth = FirebaseAuth.getInstance();
        firebaseAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
                /*usuario.
                if(usuario!=null){
                    Intent intent = new Intent(CadastroActivity.this, MainActivity.class);
                    intent.putExtra("cadastro", true);
                    startActivity(intent);
                    finish();
                }*/
            }
        };

        mSpinnerProximidade = findViewById(R.id.spinnerProximidade);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.proximidades,android.R.layout.simple_spinner_item );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerProximidade.setAdapter(adapter);
        mSpinnerProximidade.setOnItemSelectedListener(this);

        mProfileImage = (ImageView) findViewById(R.id.profileImageCadastro);
        mBotaoCadastro = (Button) findViewById(R.id.botaoCadastrar);
        mEmail = findViewById(R.id.email);
        mEmail.setText(email);
        mSenha = findViewById(R.id.senha);
        mSenha.setText(senha);
        mRadioGroupSexo = (RadioGroup) findViewById(R.id.radioGroupSexo);
        mRadioGroupOrientacao = (RadioGroup) findViewById(R.id.radioGroupOrientacao);
        mRadioGroupInteresse = findViewById(R.id.radioGroupInteresse);
        mRadioGroupTopics = findViewById(R.id.radioGroupTopics);

        mNome = findViewById(R.id.nome);
        mPhone = findViewById(R.id.phoneCadastroActivity);
        mProfileImage = (ImageView) findViewById(R.id.profileImageCadastro);
        mProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });



        mBotaoCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(resultUri==null){
                    Toast.makeText(getApplicationContext(), "Foto é Obrigatoria", Toast.LENGTH_LONG).show();
                    return;
                }
                if(!validateNome() | !validateEmail() | !validateSenha() |!validateTelefone()){
                    return;
                }
                int selectIdSexo; //= mRadioGroupSexo.getCheckedRadioButtonId();
                int selectIdOrientacao; //= mRadioGroupOrientacao.getCheckedRadioButtonId();
                int selectIdInteresse; //= mRadioGroupOrientacao.getCheckedRadioButtonId();
                int selectIdTopics; //= mRadioGroupOrientacao.getCheckedRadioButtonId();

                final RadioButton radioButtonSexo; //= (RadioButton) findViewById(selectIdSexo);
                final RadioButton radioButtonOrientacao;
                final RadioButton radioButtonInteresse;//= (RadioButton) findViewById(selectIdOrientacao);
                final RadioButton radioButtonTopics;//= (RadioButton) findViewById(selectIdOrientacao);
                try {

                    selectIdSexo = mRadioGroupSexo.getCheckedRadioButtonId();
                    selectIdOrientacao = mRadioGroupOrientacao.getCheckedRadioButtonId();
                    selectIdInteresse = mRadioGroupInteresse.getCheckedRadioButtonId();
                    selectIdTopics = mRadioGroupTopics.getCheckedRadioButtonId();

                    radioButtonSexo = (RadioButton) findViewById(selectIdSexo);
                    radioButtonOrientacao = (RadioButton) findViewById(selectIdOrientacao);
                    radioButtonInteresse = (RadioButton) findViewById(selectIdInteresse);
                    radioButtonTopics = (RadioButton) findViewById(selectIdTopics);

                    if(   radioButtonSexo.getText()       == null
                       || radioButtonOrientacao.getText() == null
                       || radioButtonInteresse.getText()  == null
                       || radioButtonTopics.getText()     == null
                            )
                    {
                        //new Alert().show(getSupportFragmentManager(),"tag");
                        Toast.makeText(getApplicationContext(), "Selecione Todos os radio buttons", Toast.LENGTH_LONG).show();
                        return;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    return;
                }
                final String email = mEmail.getEditableText().toString().trim();
                final String senha = mSenha.getEditableText().toString().trim();
                final String name =  mNome.getEditableText().toString().trim();
                final String phone = mPhone.getEditableText().toString().trim();
                //final String orientation = mOrientation.getText().toString().trim();

                mAuth.createUserWithEmailAndPassword(email, senha).addOnCompleteListener(CadastroActivity.this,
                        new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(CadastroActivity.this, "Erro ao Cadastrar email pode já estar em uso", Toast.LENGTH_SHORT).show();
                        }else{
                            String userId = mAuth.getCurrentUser().getUid();
                            currentUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                            Log.e("error","dados fora do upload task");
                            Log.e("error","nome"+ name);
                            Log.e("error","radio button Sexo"+ radioButtonSexo.getText().toString());
                            Log.e("error","telefone "+ phone);
                            Log.e("error","radio button Orientação "+ radioButtonOrientacao.getText().toString());
                            Log.e("error","radio button sex "+ radioButtonSexo.getText().toString());
                            Log.e("error","radio button interesse "+ radioButtonInteresse.getText().toString());
                            Log.e("error","radio button topico "+ radioButtonTopics.getText().toString());
                            //currentUserDb.updateChildren(userInfo);
                            if(resultUri != null){
                                StorageReference filepath = FirebaseStorage.getInstance().getReference().child("profileImage").child(userId);
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
                                        Map userInfo = new HashMap<>();
                                        userInfo.put("name", name);
                                        userInfo.put("orientation" , radioButtonOrientacao.getText().toString());
                                        userInfo.put("phone", phone);
                                        userInfo.put("profileImageUrl", downloadUrl.toString());
                                        userInfo.put("sex", radioButtonSexo.getText().toString());
                                        userInfo.put("interesse",radioButtonInteresse.getText().toString());
                                        userInfo.put("topics",radioButtonTopics.getText().toString());
                                        userInfo.put("latitude",latitude);
                                        userInfo.put("longitude",longitude);
                                        if(distancia.equals("Mundo")){
                                            dsts = "-1";
                                        }else if(distancia.equals("5 km")){
                                            dsts = "5000";
                                        }else if(distancia.equals("10 km")){
                                            dsts = "10000";
                                        }else{
                                            dsts = "15000";
                                        }
                                        userInfo.put("distancia",dsts);
                                        Log.e("error",latitude);
                                        Log.e("error",longitude);
                                        currentUserDb.updateChildren(userInfo);
                                        Intent intent = new Intent(CadastroActivity.this, MainActivity.class);
                                        intent.putExtra("cadastro", true);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            }else{
                                Toast.makeText(getApplicationContext(), "Imagem Obrigatoria", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            resultUri = data.getData();
            mProfileImage.setImageURI(resultUri);
        }
    }


    private boolean validateNome(){
        String nomeInput = mNome.getEditableText().toString().toLowerCase().trim();
        if(nomeInput.isEmpty()){
            mNome.setError("É um campo obrigatório");
            return false;
        }else if(nomeInput.length()>20){
            mEmail.setError("Nome deve conter no maximo 20");
            return true;
        }else {
            mNome.setError(null);
            return true;
        }
    }

    private boolean validateEmail(){
        String emailInput = mEmail.getEditableText().toString().toLowerCase().trim();
        if(emailInput.isEmpty()){
            mEmail.setError("É um campo obrigatório");
            return false;
        }else {
            mEmail.setError(null);
            return true;
        }
    }

    private boolean validateSenha(){
        String senhaInput = mEmail.getEditableText().toString().toLowerCase().trim();
        if(senhaInput.isEmpty()){
            mEmail.setError("É um campo obrigatório");
            return false;
        }else {
            mEmail.setError(null);
            return true;
        }
    }


    private boolean validateTelefone(){
        String telefoneInput = mPhone.getEditableText().toString().toLowerCase().trim();
        if(telefoneInput.isEmpty()){
            mPhone.setError("É um campo obrigatório");
            return false;
        }else {
            mPhone.setError(null);
            return true;
        }
    }

    private boolean validateIdade(){

        return true;
    }

    private String getAge(int year, int month, int day){
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();

        return ageS;
    }


    void configure_button() {
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.INTERNET}
                        , 10);
            }
            Log.e("sem gps","lat: "+latitude +"lng: "+ longitude);
            latitude = "false";
            longitude = "false";
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates("gps", 1000, 0, listener);

    }


    public static String getDistance(double lat_a, double lng_a, double lat_b, double lng_b) {
        // earth radius is in mile
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b - lat_a);
        double lngDiff = Math.toRadians(lng_b - lng_a);
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2)
                + Math.cos(Math.toRadians(lat_a))
                * Math.cos(Math.toRadians(lat_b)) * Math.sin(lngDiff / 2)
                * Math.sin(lngDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        int meterConversion = 1609;
        double kmConvertion = 1.6093;
        // return new Float(distance * meterConversion).floatValue();
        return distance * meterConversion + "";
        // return String.format("%.2f", distance)+" m";
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthStateListener);
    }

      @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        distancia = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(),"on item select "+ distancia,Toast.LENGTH_LONG).show();
        if(!distancia.equals("Mundo")) {
            configure_button();
            Log.e("latlng", "lat: " + latitude + " lng:" + longitude);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
