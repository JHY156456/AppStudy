package com.app.first;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final int db_version = Const.DB_VERSION;
    private static final String DB_FILE_NAME = Const.DB_FILE_NAME;

    //컬럼명 지정 title / downloadURL / 다운로드상태 / 파일현재용량 / 파일전체용량
    private static final String [] COLUMNS = {"title TEXT", "url TEXT", "status INTEGER" ,"cursize LONG","totalsize LONG"};
    private static final String TABLE_NAME = Const.DB_TABLE_NAME;

    private volatile static DBHelper helper;

    public DBHelper(Context context) {
        super(context, DB_FILE_NAME, null, db_version);
    }
    //SingleTon Pattern
    public static DBHelper getInstance(Context context) {
        if(helper==null){
            synchronized (DBHelper.class){
                if(helper == null){
                    helper = new DBHelper(context);
                }
            }
        }
        return helper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + TABLE_NAME +
                " (" + BaseColumns._ID + " integer primary key autoincrement ";
        for (int i = 0; i < COLUMNS.length; i++) {
            sql += ", " + COLUMNS[i];
        }
        sql += " ) ";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public List<ItemRow> getItem() {
        List<ItemRow> list = new ArrayList<>();
        try {
            beginTransaction();
            Cursor c = getAll();
            if (c != null) {
                int total = c.getCount();
                if (total > 0) {
                    c.moveToFirst();
                    while (!c.isAfterLast()) {
                        String title = c.getString(1);
                        String url = c.getString(2);
                        int status = c.getInt(3);
                        long curSize = c.getLong(4);
                        long totalSize = c.getLong(5);
                        list.add(new ItemRow(title, url, status, curSize, totalSize));
                        c.moveToNext();
                    }
                }
                c.close();
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            endTransaction();
        }
        return list;
    }

    public void setItem(String title, String url, int status , long curSize , long totalSize) throws SQLiteException{
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("url", url);
        values.put("status", status);
        values.put("cursize", curSize);
        values.put("totalsize", totalSize);
        insert(values);
    }

    public void setDelete() {
        AllDelete();
    }

    protected boolean isExist_DB(){
        try {
            beginTransaction();
            Cursor c = getAll();
            if (c != null) {
                int total = c.getCount();
                if (total > 0) {
                    return true;
                }
                c.close();
            }
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            endTransaction();
        }
        return false;
    }
    protected Cursor getAll() throws SQLiteException {
        return getReadableDatabase().query(TABLE_NAME, null, null, null, null, null, /*"date desc"*/null);
    }

    protected void beginTransaction() {
        getWritableDatabase().beginTransaction();
    }
    protected void endTransaction() {
        getWritableDatabase().setTransactionSuccessful();   //db속도향상
        getWritableDatabase().endTransaction();
    }

    /**@deprecated
     *
     * */
    protected void update(String contentTitle, ContentValues values) throws SQLiteException {
        getWritableDatabase().update(TABLE_NAME, values, "title=?",new String[]{contentTitle});
    }

    protected void insert(ContentValues values) throws SQLiteException {
        getWritableDatabase().insert(TABLE_NAME, null, values);
    }

    protected void AllDelete() throws SQLiteException{
        getWritableDatabase().delete(TABLE_NAME, null, null);
    }

}