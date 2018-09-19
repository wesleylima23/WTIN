package com.example.wesley.wtin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.wesley.wtin.Card.Card;
import com.example.wesley.wtin.Card.CardsArrayAdapter;
import com.example.wesley.wtin.Matches.MatchActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.wesley.wtin.CadastroActivity.getDistance;

public class MainActivity extends AppCompatActivity {

    private Card cards_data[];

    private ArrayList<String> al;
    private CardsArrayAdapter arrayAdapter;
    private int i;
    private FirebaseAuth mAuth;
    private String currentUId;
    private DatabaseReference usersDb;
    private ListView listView;
    private List<Card> rowItems;
    private String userSex;
    private String oppositeUserSex;
    private boolean NOGPS;
    private boolean cadastro = false;

    //lista de constantes usadas no firebase
    private static final String interesse = "interesse";
    private static final String orientation = "orientation";
    private static final String hetero = "Hetero";
    private static final String homo = "Homossexual";
    private static final String bi = "Bissexual";
    private static final String amigos = "Amigos";
    private static final String relacionamento = "Relacionamento";
    private double latitude, longitude;
    private double distanciaPessoal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("error","inicio da main Activity");
        Bundle extras = getIntent().getExtras();


        ////mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        //userSex = FirebaseDatabase.getInstance().getReference().child("Users").;
        mAuth = FirebaseAuth.getInstance();
        currentUId = mAuth.getCurrentUser().getUid();
        Log.e("currentUID user",currentUId);
        usersDb = FirebaseDatabase.getInstance().getReference().child("Users");
       // mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        String userSex2 = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUId).child("sex").getKey();
        Log.e("error user sex 2 ", userSex2);

        if (extras != null) {
            cadastro = extras.getBoolean("cadastro");

        }


        rowItems = new ArrayList<Card>();


        arrayAdapter = new CardsArrayAdapter(this, R.layout.item, rowItems);

        SwipeFlingAdapterView flingContainer =  (SwipeFlingAdapterView) findViewById(R.id.frame);

        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                //Log.d("LIST", "removed object!");
                try{
                    rowItems.remove(0);
                }catch (IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                Card card = (Card) dataObject;
                String userID = card.getUserID();
                //String name = card.getName();
                usersDb.child(userID).child("connections").child("nope").child(currentUId).setValue(true);
                /*cards obj = (cards) dataObject;
                String userId = obj.getUserId();
                usersDb.child(userId).child("connections").child("nope").child(currentUId).setValue(true);
                Toast.makeText(MainActivity.this, "Left", Toast.LENGTH_SHORT).show();*/

            }

            @Override
            public void onRightCardExit(Object dataObject) {
                Card card = (Card) dataObject;
                String cardID = card.getUserID();
                //String name = card.getName();
                //usersDb.child(oppositeUserSex).child(userID).child("connections").child("yepe").child(currentUId).setValue(true);
                usersDb.child(cardID).child("connections").child("yepe").child(currentUId).setValue(true);
                //usersDb.child(currentUId).child("connections").child("matches").child(cardID).setValue(true);
                isConnectionMatch(cardID);
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {

            }


            @Override
            public void onScroll(float scrollProgressPercent) {

            }

        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                intent.putExtra("nome", rowItems.get(0).getName());
                intent.putExtra("id", rowItems.get(0).getUserID());
                intent.putExtra("profileImageUrl", rowItems.get(0).getProfileImageUrl());
                intent.putExtra("orientation", rowItems.get(0).getOrientation());
                intent.putExtra("interesse", rowItems.get(0).getOrientation());
                /*
                if (extras != null) {
            nome = extras.getString("nome");
            profileImageUrl = extras.getString("profileImageUrl");
            orientation = extras.getString("orientation");
        }
                 */
                startActivity(intent);
                //Toast.makeText(MainActivity.this, "clicked",Toast.LENGTH_LONG).show();


                //TextView name = (TextView) convertView.findViewById(R.id.name);
                //ImageView image = (ImageView) convertView.findViewById(R.id.image);
                //name.setText(cardItem.getName());
            }
        });

        checkUserSex();
        /*if(cadastro){
            checkUserSex();
        }else{

        }*/

    }

    private void isConnectionMatch(String cardID) {
         DatabaseReference currentUserConnectionsDb = usersDb.child(currentUId).child("connections").child("yepe").child(cardID);
         //DatabaseReference currentUserConnectionsDb = usersDb.child(currentUId).child("connections").child("yeps").child(userId);
         currentUserConnectionsDb.addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Log.e("erro","entrou  no if ondatachange");
                    Toast.makeText(MainActivity.this,"new Connection ", Toast.LENGTH_SHORT).show();
                    String chave = FirebaseDatabase.getInstance().getReference().child("Chat").push().getKey().toString();
                    Log.e("erro",chave);
                    usersDb.child(dataSnapshot.getKey()).child("connections").child("matches").child(currentUId).child("ChatId").setValue(chave);
                    usersDb.child(currentUId).child("connections").child("matches").child(dataSnapshot.getKey()).child("ChatId").setValue(chave);
                    Log.e("erro","saindo do if ondatachange" );
                }else{
                    Log.e("erro","entrou  no else ondatachange");
                }
             }
             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }

         });
        Log.e("erro","Final do isconnection a match");
    }


    public void checkUserSex(){

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference userDb = usersDb.child(user.getUid());
        userDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("error","dentro chechkUser onDatachange  ");
                if (dataSnapshot.exists()) {

                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("sex").toString() != null) {
                        userSex = map.get("sex").toString();
                        Log.e("error", "pegando dados do usuario logado Activity main 185\n");
                        Log.e("error", "nome " + map.get("name"));
                        Log.e("error", "sex " + map.get("sex"));
                        Log.e("error", "telefone " + map.get("phone"));
                        Log.e("error", "orientacao " + map.get("orientation"));
                        Log.e("error ActivityM 201", userSex);
                        distanciaPessoal = Double.parseDouble(map.get("distancia").toString());
                        Log.e("distancia Pessoal",distanciaPessoal+"");
                        try{
                            if(!map.get("latitude").toString().equals("false")
                            || !map.get("longitude").toString().equals("false") ) {
                                latitude = Double.parseDouble(map.get("latitude").toString());
                                longitude = Double.parseDouble(map.get("longitude").toString());
                                NOGPS = false;
                            }else{
                                NOGPS = true;
                            }

                        }catch(Exception e){
                            e.printStackTrace();
                        }

                        switch (userSex) {
                            case "Male":
                                oppositeUserSex = "Female";
                                break;
                            case "Female":
                                oppositeUserSex = "Male";
                                break;
                        }
                        if (map.get("interesse").equals("Amigos")) {
                            getFriends();
                        } else if (map.get("interesse").equals("Relacionamento")) {
                            if(map.get("orientation").toString().equals("Hetero")){
                                getPeople();
                            }else if (map.get("orientation").toString().equals("Homossexual")) {

                            }else{

                            }
                        }else{
                            Log.e("error ", "entrou no else");
                        }
                    }
                }else{
                    Log.e("error","entrou no else do datasnapshot");
                    Log.e("eeror",dataSnapshot.toString());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getFriends(){
       usersDb.addChildEventListener(new ChildEventListener() {
           @Override
           public void onChildAdded(DataSnapshot dataSnapshot, String s) {
               if (dataSnapshot.exists()) {
                   double lat, lng, distance=-1;
                   boolean add = false;
                   boolean erro = false;
                   Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                   if (map.get("interesse").toString().equals("Amigos")
                       && (!dataSnapshot.child("connections").child("nope").hasChild(currentUId))
                       && (!dataSnapshot.child("connections").child("yepe").hasChild(currentUId))
                       && (!dataSnapshot.getKey().equals(currentUId))) {
                       add = false;
                       erro = false;
                       try{
                           if(!NOGPS) {
                               //latitude = Double.parseDouble(map.get("latitude").toString());
                               //longitude = Double.parseDouble(map.get("longitude").toString());

                               lat = Double.parseDouble(map.get("latitude").toString());
                               lng = Double.parseDouble(map.get("longitude").toString());
                               distance = Double.parseDouble(getDistance(latitude, longitude, lat, lng));

                               //Log.e("error", "jessica " + distance + ">");
                           }
                       }catch ( Exception e){
                           erro = true;
                           e.printStackTrace();
                       }
                       try{
                         if (distanciaPessoal == -1) {
                             if (map.get("proximidade").toString().equals("false")) {
                                 add = true;
                             } else {
                                 add = false;
                             }
                         }else {
                               if (distance <= distanciaPessoal
                                       && distance <= Double.parseDouble(map.get("distancia").toString())
                                       && distance != -1) {
                                   add = true;
                               } else {
                                   add = false;
                               }
                           }
                           if(add) {
                               Log.e("error", "pegando dados do usuario logado Activity main 185\n");
                               Log.e("error", "nome " + map.get("name"));
                               Log.e("error", "sex " + map.get("sex"));
                               Log.e("error", "telefone " + map.get("phone"));
                               Log.e("error", "orientacao " + map.get("orientation"));
                               Log.e("error", "distancia " + map.get("distancia").toString());
                               Log.e("error", "distancia pessoal cadastrada "+distanciaPessoal+">");
                               Log.e("error","distancia calculada entre os dois "+distance+">");
                               Card item = new Card(dataSnapshot.getKey(),
                                       dataSnapshot.child("name").getValue().toString(),
                                       map.get("profileImageUrl").toString(),
                                       map.get("orientation").toString(),
                                       map.get("interesse").toString()
                               );
                               rowItems.add(item);
                               arrayAdapter.notifyDataSetChanged();
                           }else{

                           }
                       }catch(Exception e){
                           e.printStackTrace();
                       }

                   }

               }

           }
           @Override
           public void onChildChanged(DataSnapshot dataSnapshot, String s) {
           }
           @Override
           public void onChildRemoved(DataSnapshot dataSnapshot) {
           }
           @Override
           public void onChildMoved(DataSnapshot dataSnapshot, String s) {
           }
           @Override
           public void onCancelled(DatabaseError databaseError) {
           }
       });
    }

    public void addItemOnArrayAdapter( Map<String, Object> map){

    }

    public void getPeople(){
        //usersDb.add
        usersDb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.child("sex").getValue() != null) {
                    if (dataSnapshot.exists()
                       && !dataSnapshot.child(interesse).getValue().toString().equals(amigos)
                       && !dataSnapshot.child("connections").child("nope").hasChild(currentUId)
                       && !dataSnapshot.child("connections").child("yepe").hasChild(currentUId)
                       && dataSnapshot.child("sex").getValue().toString().equals(oppositeUserSex)) {
                        String orientation = dataSnapshot.child("orientation").getValue().toString();
                        Log.e("erro","orientacao valor "+orientation);
                        String profileImageUrl = "default";
                        if (!dataSnapshot.child("profileImageUrl").getValue().equals("default")) {
                            profileImageUrl = dataSnapshot.child("profileImageUrl").getValue().toString();
                        }
                        Log.e("error dataSnapTo",dataSnapshot.toString());
                        Card item = new Card(dataSnapshot.getKey(),
                                dataSnapshot.child("name").getValue().toString(),
                                profileImageUrl,
                                dataSnapshot.child("orientation").getValue().toString(),
                                dataSnapshot.child("interesse").getValue().toString());
                        rowItems.add(item);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    public void logOutUsuario(View view) {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void config(View view) {

        Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
        //intent.putExtra("sex",userSex.toString());
            try {
                Log.e("user Sex 1", userSex.toString());
                intent.putExtra("sex", userSex);
                startActivity(intent);
             } catch (NullPointerException e) {
                Log.e("Erro","Valor nao recebido ainda, startActivity travado no try catch da main activity linha 332");
            }
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


    public void teste(){
        rowItems.remove(0);
    }

    public void matches(View view) {
        Intent intent = new Intent(MainActivity.this, MatchActivity.class);
        startActivity(intent);
    }
}

