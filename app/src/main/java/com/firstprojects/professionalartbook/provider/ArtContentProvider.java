package com.firstprojects.professionalartbook.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.firstprojects.professionalartbook.MainActivity;
import com.firstprojects.professionalartbook.R;

import java.util.HashMap;

import static android.content.ContentValues.TAG;


public class ArtContentProvider extends ContentProvider {


    static final String PROVIDER_NAME = "com.firstprojects.professionalartbook.provider.ArtContentProvider";
    static final String URL = "content://" + PROVIDER_NAME +"/arts";
    public static final Uri CONTENT_URI = Uri.parse(URL);

    public static final String NAME  = "name";
    public static final String IMAGE = "image";

    private static  HashMap<String,String> QUERY_PROJECTION_MAP;
    private static final int ARTS = 1;
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static{
        sURIMatcher.addURI(PROVIDER_NAME,"arts",ARTS);
    }



    //------------------ Database ------------------------//

    private SQLiteDatabase sqliteDatabase;
    public final static String SQLITE_DATABASE_NAME = "Arts";
    public final static String SQLITE_TABLE_NAME = "arts";
    public final static int SQLITE_VERSION = 1;
    public final static String CREATE_DATABASE_TABLE = "CREATE TABLE " +
            SQLITE_DATABASE_NAME +
            "(name TEXT NOT NULL ,image BLOB NOT NULL)";

    public static class SqliteHelper extends SQLiteOpenHelper{
        public SqliteHelper(@Nullable Context context) {
            super(context, SQLITE_DATABASE_NAME, null, SQLITE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DATABASE_TABLE);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE_NAME);
            onCreate(db);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            Log.i(TAG, "onDatabaseOpen: Database created!");
        }
    }

    //------------------ Database ------------------------//

    @Override
    public boolean onCreate() {
        Context context = getContext();
        SqliteHelper helper = new SqliteHelper(context);
        sqliteDatabase = helper.getWritableDatabase();
        return sqliteDatabase != null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
       SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
       QUERY_PROJECTION_MAP = new HashMap<>();
       sqLiteQueryBuilder.setTables(SQLITE_TABLE_NAME);

        switch(sURIMatcher.match(uri)){
            case ARTS:
                sqLiteQueryBuilder.setProjectionMap(QUERY_PROJECTION_MAP);
                break;
            default:

        }


        if(sortOrder != null){
            if(sortOrder.matches("")){
            sortOrder = NAME;}
        }else{
            sortOrder = NAME;
        }

        Cursor cursor = sqLiteQueryBuilder.query(sqliteDatabase,projection,selection,selectionArgs,null,null,sortOrder);
        ContentResolver contentResolver = getContext().getContentResolver();
        cursor.setNotificationUri(contentResolver,uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

      long rowID = sqliteDatabase.insert(SQLITE_TABLE_NAME,null,values);

      if(rowID != -1){
          Uri newUri = ContentUris.withAppendedId(CONTENT_URI,rowID);
          ContentResolver contentResolver = getContext().getContentResolver();
          contentResolver.notifyChange(newUri,null);
          return newUri;
      }else{

          throw new SQLiteException("FAILED THE PROCESS OF INSERT");
      }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        int rowCount = 0;

        switch(sURIMatcher.match(uri)){

            case ARTS:

              rowCount =  sqliteDatabase.delete(SQLITE_TABLE_NAME,selection,selectionArgs);
                ContentResolver contentResolver = getContext().getContentResolver();
                contentResolver.notifyChange(uri,null);

                break;

            default:

                throw new IllegalArgumentException();
        }

        return rowCount;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rowAffectedCount = 0;
        switch(sURIMatcher.match(uri)){
            case ARTS:
               rowAffectedCount = sqliteDatabase.update(SQLITE_TABLE_NAME,values,selection,selectionArgs);
               ContentResolver contentResolver = getContext().getContentResolver();
               contentResolver.notifyChange(uri,null);
                break;
            default:

                throw new IllegalArgumentException();
        }

        return rowAffectedCount;
    }
}
