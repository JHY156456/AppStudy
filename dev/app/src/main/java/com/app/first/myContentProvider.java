package com.app.first;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class myContentProvider extends ContentProvider {

    private SQLiteDatabase mDatabase;
    static final String PROVIDER_NAME = "com.app.first.myContentProvider";

    static final int GET_ALL = 1; //B앱에서 개발 Debug용으로 사용
    static final int UPDATE = 2;


    static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "getAll", GET_ALL);
        uriMatcher.addURI(PROVIDER_NAME, "updateItem", UPDATE);
    }

    @Override
    public boolean onCreate() {
        DBHelper dbHelper = new DBHelper(getContext());
        mDatabase = dbHelper.getWritableDatabase();
        return (mDatabase == null)? false:true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //읽기전용
        if(uriMatcher.match(uri) == GET_ALL) {
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(Const.DB_TABLE_NAME);
            Cursor c = qb.query(mDatabase, projection, selection, selectionArgs, null, null, null);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        }else{
            return null;
        }

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        //TODO: 다운로드하는 Content의 상태를 DB에 업데이트 구현. selection은 현재 유니크한Column인 title로..
        int id = 0;
        if(uriMatcher.match(uri) == UPDATE){
            String contentTitle =(String)values.get("title");
            id= mDatabase.update(Const.DB_TABLE_NAME,values,"title=?",new String[]{contentTitle});
        }
        return id;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

}