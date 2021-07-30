package com.firstprojects.professionalartbook;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import com.firstprojects.professionalartbook.databinding.ActivityMain2Binding;
import com.firstprojects.professionalartbook.provider.ArtContentProvider;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;


public class MainActivity2 extends AppCompatActivity {

    //declaration
    ActivityResultLauncher<Intent> activityDataResultLauncher;
    ActivityResultLauncher<String> activityPermissionResultLauncher;
    public final static String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    ActivityMain2Binding binding;

    //
    private Bitmap bitmap = null;
    private String firstName;
    private Intent intent;


    protected void definition(){

        //intent
        intent = getIntent();
        String info  = intent.getStringExtra("info");
        firstName    = intent.getStringExtra("name");
        int position = intent.getIntExtra("position",1);
        getData(info,position);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        definition();

    }

    private void getData(String info,int position){

        if(info.matches("new")){
            Bitmap background = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.ic_launcher_background);
            binding.imageView.setImageBitmap(background);
            binding.editText.setText("");
            registerActivityLauncher();
        }else{

            binding.deleteButton.setVisibility(View.VISIBLE);
            binding.updateButton.setVisibility(View.VISIBLE);
            binding.imageView.setClickable(false);
            binding.saveButton.setVisibility(View.GONE);

            //get data
            binding.editText.setText(firstName);
            binding.editText.setTextColor(ContextCompat.getColor(this,R.color.red_900));
            binding.editText.setTextSize(30f);

            MainActivity activity = new MainActivity();
            binding.imageView.setImageBitmap(MainActivity.bitmapList.get(position));
        }
    }

    public void updateButtonOnclick(View view){

        String updatedName = binding.editText.getText().toString();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ArtContentProvider.NAME,updatedName);

        ContentResolver contentResolver = getContentResolver();
        String[] selectionArgs = {intent.getStringExtra("name")};
        contentResolver.update(ArtContentProvider.CONTENT_URI,contentValues,"name=?",selectionArgs);

        Intent intent = new Intent(MainActivity2.this,MainActivity.class);
        startActivity(intent);
        finish();
    }


    public void deleteButtonOnclick(View view){


        String[] selectionArgs = {intent.getStringExtra("name")};

        ContentResolver contentResolver = getContentResolver();
        contentResolver.delete(ArtContentProvider.CONTENT_URI,"name=?",selectionArgs);

        Intent intent = new Intent(MainActivity2.this,MainActivity.class);
        startActivity(intent);
        finish();

    }

    public void saveButtonOnclick(View view) {
        String name = binding.editText.getText().toString();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();

        ContentValues contentValues = new ContentValues();
        contentValues.put(ArtContentProvider.NAME, name);
        contentValues.put(ArtContentProvider.IMAGE, bytes);

        ContentResolver contentResolver = getContentResolver();
        contentResolver.insert(ArtContentProvider.CONTENT_URI, contentValues);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }

    public void imageButtonOnclick(View view){
        if(ContextCompat.checkSelfPermission(this,READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){

            if(ActivityCompat.shouldShowRequestPermissionRationale(this,READ_EXTERNAL_STORAGE)){

                Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give permission!", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                activityPermissionResultLauncher.launch(READ_EXTERNAL_STORAGE);
                            }
                        }).show();
            }else{

                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package",this.getPackageName(),null);
                intent.setData(uri);
                startActivity(intent);
            }
            //IntentToGallery

        }else{

            activityPermissionResultLauncher.launch(READ_EXTERNAL_STORAGE);

        }
    }

    public void backButtonOnClick(View view){

        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void registerActivityLauncher(){
        activityDataResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    //get data and give it into the imageView and convert it into bitmap
                    Intent dataIntent = result.getData();
                    if(dataIntent != null){

                        Uri dataPath = dataIntent.getData();

                        try{
                            ContentResolver contentResolver = getContentResolver();
                            if(Build.VERSION.SDK_INT > 28){
                                ImageDecoder.Source source = ImageDecoder.createSource(contentResolver,dataPath);
                                bitmap = ImageDecoder.decodeBitmap(source);

                            }else{
                                bitmap = MediaStore.Images.Media.getBitmap(contentResolver,dataPath);
                            }
                            binding.imageView.setImageBitmap(bitmap);

                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        activityPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    //getusertogallery
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityDataResultLauncher.launch(intent);
                }else{
                    //showTostMessage
                    Toast.makeText(MainActivity2.this,
                            "Hey dude , you must give the permission to get an image!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}