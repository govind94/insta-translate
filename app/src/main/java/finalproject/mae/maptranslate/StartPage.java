package finalproject.mae.maptranslate;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;

import java.util.List;

public class StartPage extends AppCompatActivity {

    private static final int PERMISSION_REQUEST = 813;
    private List<String> targetLanguage;
    private List<String> targetCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        if(ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION  )!= PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission( getApplicationContext(), android.Manifest.permission.INTERNET ) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions( this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.INTERNET,Manifest.permission.CAMERA},PERMISSION_REQUEST);
        else
            start_MainActivity();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode)
        {
            case PERMISSION_REQUEST:
            {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    start_MainActivity();
                else
                {
                    Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }
                return;
            }

        }
    }

    public class task extends AsyncTask<List<String>,List<String>,List<String>>
    {
        protected List<String> doInBackground(List<String>... l) {

            LanguageCode languageCode=new LanguageCode(getApplicationContext());
            while(!languageCode.flag) ;
            targetLanguage=languageCode.getLanguageList();
            targetCode=languageCode.getLanguageCode();
            Log.d("LanguageCode","Start: "+targetCode.size());
            Intent intent=new Intent(getApplicationContext(),MainActivity.class);
            Gson gson=new Gson();
            String language=gson.toJson(targetLanguage);
            String code=gson.toJson(targetCode);
            intent.putExtra("Language List",language);
            intent.putExtra("Code List",code);
            startActivity(intent);
            finish();
            return null;
        }
    }

    private void start_MainActivity()
    {
        new task().execute(null,null,null);
    }
}
