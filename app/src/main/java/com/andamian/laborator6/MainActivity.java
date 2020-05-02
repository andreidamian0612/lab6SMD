package com.andamian.laborator6;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    final String STORAGE_OPTION = "storage";


    private boolean isUndefinedSelected() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        int storageOption = preferences.getInt(STORAGE_OPTION, 0);
        if (storageOption == 0) {
            Toast.makeText(this, "please select internal or external",
                    Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    private String getPhotoPath() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        int storageOption = preferences.getInt(STORAGE_OPTION, 0);
        String photoPath;
        if (storageOption == 1) {
            photoPath = Environment.getExternalStorageDirectory().toString();
        } else {
            photoPath = getFilesDir().getPath();
        }

        return photoPath;
    }


    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            return null;
        }
    }

    private boolean checkPerm() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        }, 1);

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final Button downloadButton = findViewById(R.id.downloadButton);
        Button loadButton = findViewById(R.id.loadButton);
        final ImageView imageView = findViewById(R.id.imageView);
        Spinner spinner = findViewById(R.id.spinner);

        final MainActivity thisActivity = this;
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(thisActivity);
        final String STORAGE_OPTION = "storage";
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPerm() || isUndefinedSelected()) {
                    return;
                }

                Runnable runnable = new Runnable() {
                    public void run() {
                        EditText editText = findViewById(R.id.urlEditText);
                        String imageURL = editText.getText().toString();
                        imageURL = "https://swarm.cs.pub.ro/~adriana/smd/android-kotlin.png"; //hardcoded because couldnt paste link in the emulator
                        Bitmap my_img = getBitmapFromURL(imageURL);
                        Log.d("STRING", getPhotoPath());
                        File im = new File(getPhotoPath(), "image.png");
                        FileOutputStream outputStream = null;
                        try {
                            outputStream = new FileOutputStream(im);
                            //does not have permission when "External" is selected
                            //and have no idea why.
                            Log.i("STRING", "The image has been saved here " + getPhotoPath() + " by " + Thread.currentThread().getName());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        assert my_img != null;
                        my_img.compress(Bitmap.CompressFormat.PNG, 90, outputStream);

                    }

                };

                new Thread(runnable).start();
            }
        });

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkPerm() || isUndefinedSelected()) {
                    return;
                }

                String photoPath = getFilesDir().getPath() + "/image.png";

                Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
                imageView.setImageBitmap(bitmap);
            }
        });

        spinner.setSelection(preferences.getInt(STORAGE_OPTION, 0));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences.Editor editor = preferences.edit();

                editor.putInt(STORAGE_OPTION, i);

                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }
}
