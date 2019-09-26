package com.example.assignment;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;

import static java.sql.DriverManager.println;

public class DBHelper {
    final static private String TAG = "DBHelper";
    String TABLE_NAME ="filelist";
    public DBHelper(){

    }
    public void createTable(SQLiteDatabase db){
        String CREATE_TABLE = "create table filelist("
                +" title text PRIMARY KEY, "
                +" content_url text, "
                +" download_status int, "
                +" current_physical_size int, "
                +" total_physical_size int);";
        db.execSQL(CREATE_TABLE);
    }
    public ArrayList<Item> getAllData(SQLiteDatabase db){
        Cursor cursor = db.rawQuery("select * from "+TABLE_NAME,null);
        int recordCount = cursor.getCount();
        ArrayList<Item> records = new ArrayList<Item>();
        for(int i=0; i<recordCount; i++){
            cursor.moveToNext();
            String title = cursor.getString(0);
            String contentURL = cursor.getString(1);
            int downloadStatus = cursor.getInt(2);
            int currentPhysicalSize = cursor.getInt(3);
            int totalPhysicalSize = cursor.getInt(4);
            records.add(new Item(title,contentURL,downloadStatus,currentPhysicalSize,totalPhysicalSize));

        }
        return records;
    }
    public void insertRecordParam(Item curItem, SQLiteDatabase db) {
        println("inserting records using parameters.");

        ContentValues recordValues = new ContentValues();
        recordValues.put("title", curItem.getTitle());
        recordValues.put("content_url", curItem.getContentURL());
        recordValues.put("download_status",curItem.getDownloadStatus());
        recordValues.put("current_physical_size", curItem.getCurrentPhysicalSize());
        recordValues.put("total_physical_size", curItem.getTotalPhysicalSize());
        db.insert("filelist", null, recordValues);

    }
    public void executeRawQuery(SQLiteDatabase db) {
        println("\nexecuteRawQuery called.\n");

        Cursor c1 = db.rawQuery("select count(*) as Total from filelist", null);
        Log.d(TAG, "cursor count : " + c1.getCount());
        c1.moveToNext();

        Log.d(TAG, "record count : " + c1.getInt(0));
        c1.close();

    }
    public boolean isExistTable(SQLiteDatabase db){

        try{
            db.rawQuery("SELECT * FROM filelist",null);

        }catch(SQLiteException e){
            e.printStackTrace();
            return false;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return  true;
    }
    public void statusChange(String title,int status,SQLiteDatabase db){
        println("updating records using parameters.");

        ContentValues recordValues = new ContentValues();
        recordValues.put("download_status", status);
        String[] whereArgs = {title};

        db.update("filelist",
                recordValues,
                "title = ?",
                whereArgs);

    }
}
