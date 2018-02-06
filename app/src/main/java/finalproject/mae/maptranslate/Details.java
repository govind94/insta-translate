package finalproject.mae.maptranslate;

import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import finalproject.mae.maptranslate.ImageTranslation.TranslationFB;

public class Details extends AppCompatActivity {
    DatabaseReference mDatabase;
    StorageReference mStorage;
    private ArrayAdapter<TranslationFB> adapter;
    private ArrayList<TranslationFB> info_list=new ArrayList<>();
    private double marker_Lat;
    private double marker_Lng;
    private String targetLanguage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        targetLanguage=getIntent().getStringExtra("Targetlanguage");
        marker_Lat=getIntent().getDoubleExtra("Lat",0);
        marker_Lng=getIntent().getDoubleExtra("Lng",0);



        adapter=new ArrayAdapter<TranslationFB>(this,R.layout.custom_info_window,info_list)
        {
            @Override
            public View getView(int pos, View convertView, ViewGroup parent)
            {
                convertView=View.inflate(getContext(),R.layout.custom_info_window, null);
                TextView textView=convertView.findViewById(R.id.markerText);
                final ImageView imageView=convertView.findViewById(R.id.markerImage);
                textView.setText(info_list.get(pos).getTranslatedText());
                Log.d("myfilter", "ImageName0: " + info_list.get(pos).imageName());
                if(imageView.getTag().equals("123")) {
                    imageView.setTag("000");
                    mStorage.child("images/" + info_list.get(pos).imageName()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("myfilter", "SUCCESS!!!");
                            Picasso.with(Details.this).load(uri).into(imageView);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            Log.d("myfilter", "FAIL!!!");
                            imageView.setImageAlpha(android.R.drawable.sym_def_app_icon);
                        }
                    });
                }
                return convertView;
            }
        };

        ListView listView=findViewById(R.id.infowindow_list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                ImageView imageView=view.findViewById(R.id.markerImage);
                ImagePopup imagePopup = new ImagePopup(view.getContext());
                imagePopup.setWindowWidth(Resources.getSystem().getDisplayMetrics().widthPixels);
                imagePopup.setWindowHeight(Resources.getSystem().getDisplayMetrics().heightPixels);
                imagePopup.setBackgroundColor(Color.BLACK);
                imagePopup.setHideCloseIcon(true);
                imagePopup.setImageOnClickClose(true);
                imagePopup.initiatePopup(imageView.getDrawable());
                imagePopup.viewPopup();

            }
        });


        Query query=mDatabase.getRef();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot translations:dataSnapshot.getChildren())
                {
                    TranslationFB translationFB=translations.getValue(TranslationFB.class);
                    double info_Lat = translationFB.getLatitude();
                    double info_Lng = translationFB.getLongitude();
                    String imageName = translations.getKey();
                    translationFB.putImageName(imageName);
                    float results[]=new float[1];
                    Log.d("myfilter", "ImageName1: " + translations.getKey());
                    Location.distanceBetween(info_Lat,info_Lng,marker_Lat,marker_Lng,results);
                    if(results[0]<=10 && translationFB.getTargetLanguage().equals(targetLanguage))
                    {
                        info_list.add(translationFB);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }



}
