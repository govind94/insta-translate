package finalproject.mae.maptranslate;
/**
 * Final Project for Mobile App Engineering
 */

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import finalproject.mae.maptranslate.ImageTranslation.RETCONSTANT;
import finalproject.mae.maptranslate.ImageTranslation.TranslationActivity;
import finalproject.mae.maptranslate.ImageTranslation.TranslationFB;

import android.widget.Spinner;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.cloud.translate.Translation;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity
        implements OnMapReadyCallback,GoogleMap.OnMapLoadedCallback, View.OnClickListener, AdapterView.OnItemSelectedListener,TakePicFragment.OnFragmentInteractionListener,ChildEventListener{
    private FusedLocationProviderClient fusedLocationClient;
    private double current_Lat;
    private double current_Lng;
    private GoogleMap mMap;
    public String targetLanguage;
    DatabaseReference mDatabase;
    StorageReference mStorage;
    private List<String> targetCode;
    private List<String> languageList;
    private ArrayList<Marker> marker_list=new ArrayList<Marker>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mf = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mf.getView().setClickable(true);
        mf.getMapAsync(this);



    }

    @Override
    public void onMapReady(GoogleMap gmap)
    {
        ui_initialize();
        map_initialize(gmap);
        location_initialize();
        database_initialize();
        mMap.setOnMapLoadedCallback(this);
    }

    @Override
    public void onMapLoaded()
    {
        ;
    }

    //Initialize spinner and target language
    private void ui_initialize()
    {
        Log.d("ui_initialize","initializing the User Interface");
        Spinner choose_targlang=findViewById(R.id.choose_targlang);

        Gson gson=new Gson();
        Intent intent=getIntent();
        String language=intent.getStringExtra("Language List");
        String code=intent.getStringExtra("Code List");
        languageList= gson.fromJson(language,new ArrayList<String>().getClass());
        targetCode=gson.fromJson(code,new ArrayList<String>().getClass());
        Log.d("Size of Language List","" + languageList.size());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,languageList);
        choose_targlang.setAdapter(adapter);
        choose_targlang.setOnItemSelectedListener(this);
        SharedPreferences load_pref = getSharedPreferences("TargetLanguage",MODE_PRIVATE);
        targetLanguage = load_pref.getString(RETCONSTANT.SHAREDPREFTARGETLANG,"en");
        Log.d("Loaded from Preferences", targetLanguage);
        int index= targetCode.indexOf(targetLanguage);
        if(index==-1)
            index=targetCode.indexOf("en");
        choose_targlang.setSelection(index);
        ImageButton picChooser = (ImageButton)findViewById(R.id.takePicButton);
        picChooser.setOnClickListener(this);
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        Log.d("Spinner", "Item:" + position + " selected!");
        SharedPreferences prefs = getSharedPreferences("TargetLanguage", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        targetLanguage = targetCode.get(position);
        prefsEditor.putString(RETCONSTANT.SHAREDPREFTARGETLANG, targetLanguage);
        prefsEditor.commit();
        Log.d("Target Language Code", targetLanguage);

        ArrayList<Marker> temp=new ArrayList<Marker>(marker_list);
        marker_list.clear();
        update_markers(current_Lat,current_Lng);
        for(Marker marker:temp)
            marker.remove();
    }

    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void map_initialize(GoogleMap map)
    {
        Log.d("map_initialize", "Initializing map settings");
        mMap=map;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        try
        {
            mMap.setMyLocationEnabled(true);
        }
        catch (SecurityException e)
        {
            Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent=new Intent(getApplicationContext(),Details.class);
                intent.putExtra("Lat",marker.getPosition().latitude);
                intent.putExtra("Lng",marker.getPosition().longitude);
                intent.putExtra("Targetlanguage",targetLanguage);
                startActivity(intent);
                return true;
            }
        });



    }

    private void database_initialize()
    {
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addChildEventListener(this);
    }

    private void location_initialize()
    {
        try
        {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if(location!=null)
                    {
                        get_current_location(location);
                        addMarkers(location);
                    }
                }
            });
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(1000);
            locationRequest.setFastestInterval(500);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationCallback locationCallback=new LocationCallback()
            {
                @Override
                public void onLocationResult(final LocationResult locationResult)
                {
                    for(Location location: locationResult.getLocations())
                    {
                        if(location!=null)
                        {
                            get_current_location(location);
                            ArrayList<Marker> temp=new ArrayList<Marker>(marker_list);
                            marker_list.clear();
                            addMarkers(location);
                            for(Marker marker:temp)
                                marker.remove();
                        }

                        else
                            break;
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,null);
        }
        catch (SecurityException e)
        {
            Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }

    }


    private void get_current_location(Location loc)
    {
        current_Lat = Double.parseDouble(new DecimalFormat("#0.000000").format(loc.getLatitude()));
        current_Lng = Double.parseDouble(new DecimalFormat("#0.000000").format(loc.getLongitude()));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(current_Lat,current_Lng),15,0,0)));
    }

    public void onClick(View v){
        if(v.getId() == R.id.takePicButton){
            TakePicFragment picFragment = new TakePicFragment();
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.framePicChooser,picFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }


    public void FragmentResponse(int flag){
        if(flag == RETCONSTANT.GALLERY){
            galleryIntent();
        }
        else if(flag == RETCONSTANT.CAMERA){
            cameraIntent();
        }
    }

    public void cameraIntent(){
        Log.d("Camera Intent","Response Recieved");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "finalproject.mae.maptranslate",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, RETCONSTANT.CAMERA);
            }
        }
    }

    public void galleryIntent(){
        Log.d("Gallery Intent","Response Recieved");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RETCONSTANT.GALLERY);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        /**
         * Currently both conditions execute the same code
         * I was trying to implement real time text translation
         * that would show up on the screen. If I have time, I'll try again
         * but for now, this allows the user to take picture
         * from gallery and camera (the behavior was super buggy and device
         * dependant before)
         */
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("onActivityResult","Checking conditions");
        if(requestCode == RETCONSTANT.GALLERY && resultCode == RESULT_OK){
            Log.d("URI location", data.getData().toString());
            Intent actIntent = new Intent(this, TranslationActivity.class);
            actIntent.putExtra(RETCONSTANT.CURRLONG, current_Lng);
            actIntent.putExtra(RETCONSTANT.CURRLAT, current_Lat);
            actIntent.putExtra(RETCONSTANT.IMAGEURI, data.getData().toString());
            if(targetLanguage == null || targetLanguage.equals("")){
                Log.d("TargetLanguage ERROR", "Custom target language not found!");
                targetLanguage = "de"; //german for demo purposes
            }
            Log.d("TargetLanguage",targetLanguage);
            actIntent.putExtra(RETCONSTANT.TARGETLANG,targetLanguage);
            startActivity(actIntent);
        }
        else if(requestCode == RETCONSTANT.CAMERA && resultCode == RESULT_OK){
            Uri imageUri = Uri.fromFile(new File(mCurrentPhotoPath));
            Intent actIntent = new Intent(this, TranslationActivity.class);
            actIntent.putExtra(RETCONSTANT.CURRLONG, current_Lng);
            actIntent.putExtra(RETCONSTANT.CURRLAT, current_Lat);
            actIntent.putExtra(RETCONSTANT.IMAGEURI, imageUri.toString());
            if(targetLanguage == null || targetLanguage.equals("")){
                Log.d("TargetLanguage ERROR", "Custom target language not found!");
                targetLanguage = "de"; //german for demo purposes
            }
            Log.d("TargetLanguage",targetLanguage);
            actIntent.putExtra(RETCONSTANT.TARGETLANG,targetLanguage);
            startActivity(actIntent);
        }

    }

    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        finish();
        System.exit(0);
    }

    private void addMarkers(Location location){
        final double lat=location.getLatitude();
        final double lng=location.getLongitude();

        update_markers(lat,lng);

    }


    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        TranslationFB translationFB = dataSnapshot.getValue(TranslationFB.class);
        double marker_Lat = translationFB.getLatitude();
        double marker_Lng = translationFB.getLongitude();
        final LatLng LL = new LatLng(marker_Lat,marker_Lng);
        float results[]=new float[1];
        Location.distanceBetween(current_Lat,current_Lng,marker_Lat,marker_Lng,results);
        if(results[0]<=100 && translationFB.equals(targetLanguage))
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean merged=false;

                    for(Marker marker:marker_list)
                    {
                        double lat=marker.getPosition().latitude;
                        double lng=marker.getPosition().longitude;
                        float[] result=new float[1];
                        Location.distanceBetween(lat,lng,LL.latitude,LL.longitude,result);
                        if(result[0]<=10)
                        {
                            merged = true;
                            break;
                        }
                    }

                    if(!merged)
                    {
                        Marker marker=mMap.addMarker(new MarkerOptions().position(LL));
                        marker_list.add(marker);
                    }
                }
            }).run();
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

    private void update_markers(final double lat,final double lng)
    {
        Query query=mDatabase.getRef();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot translate:dataSnapshot.getChildren())
                {
                    TranslationFB translationFB = translate.getValue(TranslationFB.class);
                    double marker_Lat = translationFB.getLatitude();
                    double marker_Lng = translationFB.getLongitude();
                    final LatLng LL = new LatLng(marker_Lat,marker_Lng);
                    float results[]=new float[1];
                    Location.distanceBetween(lat,lng,marker_Lat,marker_Lng,results);
                    if(results[0]<=100 && translationFB.getTargetLanguage().equals(targetLanguage))
                    {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean merged=false;

                                for(Marker marker:marker_list)
                                {
                                    double lat=marker.getPosition().latitude;
                                    double lng=marker.getPosition().longitude;
                                    float[] result=new float[1];
                                    Location.distanceBetween(lat,lng,LL.latitude,LL.longitude,result);
                                    if(result[0]<=10)
                                    {
                                        merged = true;
                                        break;
                                    }
                                }

                                if(!merged)
                                {
                                    Marker marker=mMap.addMarker(new MarkerOptions().position(LL));
                                    marker_list.add(marker);
                                }
                            }
                        }).run();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
