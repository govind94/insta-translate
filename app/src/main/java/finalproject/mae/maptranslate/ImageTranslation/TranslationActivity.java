package finalproject.mae.maptranslate.ImageTranslation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import finalproject.mae.maptranslate.MainActivity;
import finalproject.mae.maptranslate.R;
import finalproject.mae.maptranslate.StartPage;


public class TranslationActivity extends AppCompatActivity implements View.OnClickListener {

    DatabaseReference mDatabase;
    StorageReference mStorage;
    
    String targetLanguage;
    TextView origin;
    TextView translation;
    TextView translatedTargetLang;

    Bitmap originalImage;
    Uri imageUri;
    String extractedText;
    String translatedText;
    TextView detectText;
    String language;

    double currentLongitude;
    double currentLatitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation);

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Button goodButton = (Button) findViewById(R.id.goodbutt);
        Button badButton = (Button) findViewById(R.id.badbutt);
        goodButton.setOnClickListener(this);
        badButton.setOnClickListener(this);
        origin = (TextView) findViewById(R.id.origText);
        translation = (TextView) findViewById(R.id.translationText);
        translatedTargetLang = (TextView) findViewById(R.id.TranslatedTextLang);
        ImageView image = (ImageView) findViewById(R.id.translationPic);
        detectText = (TextView) findViewById(R.id.detectText);
        currentLatitude = getIntent().getDoubleExtra(RETCONSTANT.CURRLAT, 0);
        currentLongitude = getIntent().getDoubleExtra(RETCONSTANT.CURRLONG, 0);
        imageUri = Uri.parse(getIntent().getStringExtra(RETCONSTANT.IMAGEURI));
        try {
            originalImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            image.setImageBitmap(originalImage);
        }
        catch(IOException e){
            e.printStackTrace();
        }
        extractedText = "";
        translatedText = "";
        targetLanguage = getIntent().getStringExtra(RETCONSTANT.TARGETLANG);
        Log.d("TranslationActivity","Target Language = " +targetLanguage);
        language = "";
        extractTextFromImage();

    }

    public void extractTextFromImage() {
        TextRecognizer textRecognizer = new TextRecognizer.Builder(this).build();
        if (!textRecognizer.isOperational()) {
            Log.d("myfilter", "Detector dependencies are not available yet");
            return;
        }
        Frame imageFrame = new Frame.Builder().setBitmap(originalImage).build();
        SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
        for (int i = 0; i < textBlocks.size(); i++) {
            TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
            extractedText = extractedText + textBlock.getValue();
            Log.d("myfilter", "String: " + extractedText);
        }
        extractedText = extractedText.trim().replaceAll("\\n", " ");
        origin.setText(extractedText);
        origin.setMovementMethod(new ScrollingMovementMethod());
        translateText();
    }

    public void translateText() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://translation.googleapis.com/language/translate/v2";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Response is", response);
                try {
                    JSONObject mainObject = new JSONObject(response);
                    JSONObject dataobj = mainObject.getJSONObject("data");
                    JSONArray translationArray = dataobj.getJSONArray("translations");
                    JSONObject translationObj = translationArray.getJSONObject(0);
                    translatedText =  translationObj.getString("translatedText");
                    String srcLang = translationObj.getString("detectedSourceLanguage");
                    detectText.setText("Original Text Language: " + srcLang);
                    translation.setText(translatedText);
                    translation.setMovementMethod(new ScrollingMovementMethod());
                    translatedTargetLang.setText("Translated Text Language: " + targetLanguage);

                } catch (org.json.JSONException e) {
                    e.printStackTrace();
                    Log.d("JSON", "Could not parse json string");
                    return;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Volley", "Error on response");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("q", extractedText);
                params.put("target", targetLanguage);
                params.put("key", RETCONSTANT.API_KEY);
                return params;
            }
        };
        queue.add(stringRequest);

    }



    public void onClick(View v) {
        if (v.getId() == R.id.badbutt) {
            //return to map activity
            Log.d("onClick", "Bad Button Pressed. Start map activity");
            //            Intent intent = new Intent(this, StartPage.class);
//            startActivity(intent);
            finish();

        } else if (v.getId() == R.id.goodbutt) {
            Log.d("onClick", "Good Button Pressed. Add to databse, start map activity");
            //TODO: add the following params to database
            //Bitmap originalImage
            //String translatedText
            //String targetLanguage
            //Double currentLongitude
            //Double currentLatitude
            
            addToFirebaseDatabase();


            //return to map activity
//            Intent intent = new Intent(this, StartPage.class);
//            startActivity(intent);
            finish();

        }
    }
    
    private void addToFirebaseDatabase() {
        String id = mDatabase.push().getKey(); // Unique id (primary key)
        Log.d("myfilter", "ID: " + id);
        TranslationFB translation = new TranslationFB(targetLanguage, currentLatitude, currentLongitude, id, translatedText);
        mDatabase.child(id).setValue(translation); // Add to DB

        storeImageinStorage(id); // Add image to Storage
        Toast.makeText(this, "Database updated successfully.", Toast.LENGTH_LONG).show();
    }

    private void storeImageinStorage(String id) {
        StorageReference path = mStorage.child("images").child(id);
        path.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(), "Storage updated successfully.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}


