package com.fontys.practical;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import com.fontys.practical.Authentication.Login;
import com.fontys.practical.Authentication.Register;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener {
    private double lat;
    private double lng;
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    LocationRequest mRequest;
    GoogleApiClient mClient;

    //Firebase Variables
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;
    private DatabaseReference tokenRef;
    public final static String AUTH_KEY_FCM = "AIzaSyDJuLVtqt05VYeiscmE4Fyrcsw2hB3Blc8";
    public final static String API_URL_FCM = "https://fcm.googleapis.com/fcm/send";
    private ValueEventListener valueEventListener;
    private ValueEventListener NotificationEventListner;
    private int sent=0;
    private int minutes=2;
    Timer timer;

    Map<String, Marker> mMarkers;

    //UI Variables
    private Menu mToolbarMenu;

     List<String> listT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();
        usersRef = firebaseDatabase.getReference("Users");
        tokenRef = firebaseDatabase.getReference("Tokens");
        firebaseAuth = FirebaseAuth.getInstance();



        mMarkers = new HashMap<String,Marker>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        listT = new ArrayList<String>();
        usersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                UsersLocations tokens = dataSnapshot.getValue(UsersLocations.class);
                System.out.println("The updated post title is: " + tokens.getToken());
                listT.add(tokens.getToken());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
//        if() {
//            accessToken();
//        }
        NotificationToken();



    }

    protected synchronized void buildGoogleApiClient() {
        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //Location Permission already granted
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        } else {
            //Request Location Permission
            askLocationPermission();
        }

        mMap.setOnMarkerClickListener(this);
    }

    // Inflate top right menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mToolbarMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        updateUI(firebaseAuth.getCurrentUser());
        return true;
    }

    // When user clicks a top right menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LayoutInflater inflater = this.getLayoutInflater();
        switch (item.getItemId()) {
            case R.id.action_login:
                Login loginDialog = new Login();
                loginDialog.show(getSupportFragmentManager(), "Login");
                return true;
            case R.id.action_register:
                Register registerDialog = new Register();
                registerDialog.show(getSupportFragmentManager(), "Register");
                return true;
            case R.id.action_logout:
                usersRef.child(firebaseAuth.getCurrentUser().getUid()).child("loggedIn").setValue(false)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                task.isSuccessful();
                            }
                        });
                firebaseAuth.signOut();
                updateUI(firebaseAuth.getCurrentUser());
                return true;
            case R.id.action_sensor:
                startActivity(new Intent(this, SensorActivity.class));
            case R.id.f_token:
                NotificationToken();
                ///
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();

        if (firebaseAuth.getCurrentUser() != null) {
            String userEmail = firebaseAuth.getCurrentUser().getEmail();
            String userID = userEmail.substring(0, userEmail.indexOf("@"));

            String deviceToken=NotificationToken();
            UsersLocations usersLocation = new UsersLocations(userID, lat, lng,deviceToken);

            usersRef.child(firebaseAuth.getCurrentUser().getUid()).setValue(usersLocation)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            task.isSuccessful();
                        }
                    });

        }
    }

    //get the location updates
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mRequest = new LocationRequest();
        mRequest.setInterval(1000);
        mRequest.setFastestInterval(1000);
        mRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mClient, mRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void updateUI(final FirebaseUser user) {
        if (user != null) {
            final String userEmail = user.getEmail();
            this.setTitle(userEmail);
            mToolbarMenu.findItem(R.id.action_logout).setVisible(true);
            mToolbarMenu.findItem(R.id.action_register).setVisible(false);
            mToolbarMenu.findItem(R.id.action_login).setVisible(false);

            // Add the database value change listener
            valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        UsersLocations userInfo = ds.getValue(UsersLocations.class);
//                        UserToken userToken=ds.getValue(UserToken.class);
                        String markerId = ds.getKey();
                        Marker marker = mMarkers.get(markerId);

                        if(userInfo.loggedIn && !userInfo.id.equals(userEmail.substring(0, userEmail.indexOf("@")))) {
                            double latitude = userInfo.getLatitude();
                            double longitude = userInfo.getLongitude();
                            final String deviceToken=userInfo.getToken();


                            LatLng location = new LatLng(latitude, longitude);

                            if (marker == null) {
                                marker = mMap.addMarker(new MarkerOptions().title(userInfo.id).position(location));

                            } else {
                                marker.setPosition(location);
                            }
                            mMarkers.put(markerId, marker);

                            final float distance[] = new float[10];
                            Location.distanceBetween(lat, lng, marker.getPosition().latitude, marker.getPosition().longitude, distance);


                                    if ((distance[0]/1000)<450){


                                        System.out.println("retrieved token" + deviceToken);
                                        for (String to : listT) {
                                            try {

                                               final Notification sendPushNotification = new Notification(MainActivity.this, to);
                                                new java.util.Timer().schedule(new TimerTask(){
                                                    @Override
                                                    public void run() {
                                                        System.out.println("Executed...");

                                                        //1000*5=5000 mlsec. i.e. 5 seconds. u can change accordngly
                                                        sendPushNotification.execute();
                                                    }
                                                },1000*60,1000*60);


                                                sent=1;

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }



                                    }
                                    }else if((distance[0]/1000)>450){
                                        sent=0;

                                    }




                        } else if(!userInfo.loggedIn && marker != null) {
                            marker.remove(); // remove from map
                            mMarkers.remove(markerId); // remove from hashmap
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    throw databaseError.toException();
                }
            };
            usersRef.addValueEventListener(valueEventListener);
        } else {
            this.setTitle("Map");
            mToolbarMenu.findItem(R.id.action_register).setVisible(true);
            mToolbarMenu.findItem(R.id.action_login).setVisible(true);
            mToolbarMenu.findItem(R.id.action_logout).setVisible(false);
            mMap.clear();
            if(valueEventListener != null)
                usersRef.removeEventListener(valueEventListener);
        }
    }


    // REQUEST LOCATION PERMISSION
    public static final int REQUEST_LOCATION = 1;
    private void askLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // show the explanation to get the permission granted by user
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] resultArr) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (resultArr.length > 0
                        && resultArr[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {
                    //Permission denied
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        float distance[] = new float[10];
        Location.distanceBetween(lat, lng, marker.getPosition().latitude, marker.getPosition().longitude, distance);
        marker.setSnippet("Distance = "+distance[0]/1000 + " KM");
        return false;
    }
    ///Push notification


    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
//    @Override
//    public void onNewToken(String token) {
//        Log.d("My Tag", "Refreshed token: " + token);
//
//        // If you want to send messages to this application instance or
//        // manage this apps subscriptions on the server side, send the
//        // Instance ID token to your app server.
//        sendRegistrationToServer(token);
//    }
    public void accessToken(){
        new AlertDialog.Builder(this)
                .setTitle("Notification Permission Needed")
                .setMessage("This app needs the Notification permission")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String token = FirebaseInstanceId.getInstance().getToken();
                        Log.d("MYTAG", "This is your Firebase token" + token);
                        usersRef.child(firebaseAuth.getCurrentUser().getUid()).setValue(token)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        task.isSuccessful();
                                    }
                                });
                    }
                })
                .create()
                .show();
    }
    public String NotificationToken (){

                String newToken=FirebaseInstanceId.getInstance().getToken();
//                Log.e("newToken", newToken);

               System.out.println("TOKEN:"+newToken);
               return newToken;


            }
//        });

   // }
   public class Notification extends AsyncTask<Void, Void, Void> {
       private final String FIREBASE_URL = "https://fcm.googleapis.com/fcm/send";
       private final String SERVER_KEY = "AAAA6krFWAU:APA91bGmvWB44U_zBlPtTpEsEtcJ8T60aQwd6FNSI1kMyB0JASX4k7Ec7cqFEr1_TnWCWX6RJVk-Z6v44Ioe9Z5qljnKOORFzpe_SzAJJh8NbIcSaEiOW232xSJ-czxndVPXlYoo_3pz";
       private Context context;
       private String token;

       public Notification(){}
       public Notification(Context context, String token) {
           this.context = context;
           this.token = token;
       }

       @Override
       protected Void doInBackground(Void... voids) {


           try {
               URL url = new URL(FIREBASE_URL);
               HttpURLConnection connection = (HttpURLConnection) url.openConnection();

               connection.setUseCaches(false);
               connection.setDoInput(true);
               connection.setDoOutput(true);

               connection.setRequestMethod("POST");
               connection.setRequestProperty("Content-Type", "application/json");
               connection.setRequestProperty("Accept", "application/json");
               connection.setRequestProperty("Authorization", "key=" + SERVER_KEY);

               JSONObject root = new JSONObject();
               root.put("to", token);

               JSONObject data = new JSONObject();

               data.put("title", "Android");
               data.put("body", "Someone is closer to you");

//               JSONObject innerData = new JSONObject();
//               innerData.put("key", "Extra data");
//               data.put("data", innerData);
               root.put("notification", data);
               Log.e("PushNotification", "Data Format: " + root.toString());

               try {
                   OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
                   writer.write(root.toString());
                   writer.flush();
                   writer.close();

                   int responseCode = connection.getResponseCode();
                   Log.e("PushNotification", "Request Code: " + responseCode);

                   BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((connection.getInputStream())));
                   String output;
                   StringBuilder builder = new StringBuilder();
                   while ((output = bufferedReader.readLine()) != null) {
                       builder.append(output);
                   }
                   bufferedReader.close();
                   String result = builder.toString();
                   Log.e("PushNotification", "Result JSON: " + result);
               } catch (Exception e) {
                   e.printStackTrace();
                   Log.e("PushNotification", "Error: " + e.getMessage());
               }

           } catch (Exception e) {
               e.printStackTrace();
               Log.e("PushNotification", "Error: " + e.getMessage());
           }

           return null;
       }
   }
}
