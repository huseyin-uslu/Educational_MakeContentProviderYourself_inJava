package com.firstprojects.professionalartbook;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.firstprojects.professionalartbook.databinding.ActivityMainBinding;
import com.firstprojects.professionalartbook.provider.ArtContentProvider;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_art,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add_art_item){
            Intent intent = new Intent(MainActivity.this,MainActivity2.class);
            intent.putExtra("info","new");
           startActivity(intent);
                   finish();
        }
        return super.onOptionsItemSelected(item);
    }


    //----------- Declaration ----------------//
    ActivityMainBinding binding = null;
    ArrayList<String> nameList;
   public static ArrayList<Bitmap> bitmapList;
    ArrayAdapter<String> adapter;

    protected void definition(){
        nameList = new ArrayList<>();
        bitmapList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,nameList);
        binding.ListViewShowingData.setAdapter(adapter);


        getData(); //getDataFromSqlite

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getLayoutInflater();
        binding = ActivityMainBinding.inflate(inflater);
        setContentView(binding.getRoot());
        definition(); //INITIALIZE//
    }

    protected void getData(){
        try{

            ContentResolver contentResolver = getContentResolver();

            String url = "content://" + getResources().getString(R.string.provider_authorities);
            Uri uri = Uri.parse(url);//arts'ı falan kendinisi ayarlayacak.

            Cursor cursor = contentResolver.query(uri,null,null,null,ArtContentProvider.NAME);

            if(cursor != null){

                int nameColumnIX = cursor.getColumnIndex(ArtContentProvider.NAME);
                int imageColumnIX = cursor.getColumnIndex(ArtContentProvider.IMAGE);

                while(cursor.moveToNext()){

                    String name = cursor.getString(nameColumnIX);
                    nameList.add(name);

                    byte[] bytes = cursor.getBlob(imageColumnIX);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    bitmapList.add(bitmap);

                    adapter.notifyDataSetChanged();

                }
                cursor.close();
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "getData: ",e);
        }

        binding.ListViewShowingData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,MainActivity2.class);


                String name = nameList.get(position);
                intent.putExtra("name",name);
                intent.putExtra("position",position);
                intent.putExtra("info","old");
                startActivity(intent);
            }
        });

    }
}