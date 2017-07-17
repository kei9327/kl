package com.knowlounge.sqllite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.knowlounge.model.FriendUser;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by we160303 on 2016-07-21.
 */
public class DbOpenHelper {

    private static final String DATABASE_NAME = "Knowlounge.db";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mContext;

    private class DatabaseHelper extends SQLiteOpenHelper{

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
            super(context, name, factory, version, errorHandler);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DataBases.CreateDB._CREATE);
            db.execSQL(DataBases.CreateDB._SEARCH_KEYWORD_CREATE);
        }

        public void onCreate(SQLiteDatabase db, String tableName){
            db.execSQL(DataBases.CreateDB._CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+ DataBases.CreateDB._TABLENAME);
            db.execSQL("DROP TABLE IF EXISTS "+ DataBases.CreateDB._SEARCH_KEYWORD_TABLE);
            onCreate(db);
        }
    }


    public DbOpenHelper(Context context){
        this.mContext = context;
    }


    public DbOpenHelper open(String tableName) throws SQLException{
        mDBHelper = new DatabaseHelper(mContext, tableName, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }


    public void insert(FriendUser friendUser){
        Log.d("DB", "insert");
        mDB = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.NAME,friendUser.getUserNm());
        values.put(DataBases.CreateDB.USER_ID,friendUser.getId());
        values.put(DataBases.CreateDB.THUMBNAIL,friendUser.getUserThumbnail());
        mDB.insert(DataBases.CreateDB._TABLENAME, null, values);
    }


    public void insert(SearchKeyword keyword){
        Log.d("DB", "insert");
        mDB = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DataBases.CreateDB.SK_USER_ID,keyword.getUserId());
        values.put(DataBases.CreateDB.SK_KEYWORD,keyword.getKeyword());
        mDB.insert(DataBases.CreateDB._SEARCH_KEYWORD_TABLE, null, values);
    }


    public void dropAndReCreateTable(String tableName){
        Log.d("DB", "drop table");
        mDB = mDBHelper.getWritableDatabase();
        mDB.execSQL("DROP TABLE IF EXISTS "+ tableName);
        mDBHelper.onCreate(mDB,tableName);
    }


    public ArrayList<SearchKeyword> getAllSearchKeyword(String userId){
        Log.d("DB", "getAllSearchKeyword");
        ArrayList<SearchKeyword> searchKeywords = new ArrayList<SearchKeyword>();

        String selectAllQuery = "SELECT * FROM " + DataBases.CreateDB._SEARCH_KEYWORD_TABLE;

        mDB = mDBHelper.getWritableDatabase();
        Cursor cursor = mDB.rawQuery(selectAllQuery, null);

        if(cursor.moveToFirst()){
            do {
                String id = cursor.getString(cursor.getColumnIndex(DataBases.CreateDB.SK_USER_ID));
                String keyword = cursor.getString(cursor.getColumnIndex(DataBases.CreateDB.SK_KEYWORD));
                SearchKeyword data = new SearchKeyword(id, keyword );

                if(id.equals(userId))
                    searchKeywords.add(data);

                Log.d("search_keyword", data.getKeyword());
            } while (cursor.moveToNext());
        }
        Collections.reverse(searchKeywords);
        return searchKeywords;

    }

    public ArrayList<FriendUser> getAllRow(){
        ArrayList<FriendUser> friendList = new ArrayList<FriendUser>();

        //Select All Query
        String selectAllQuery = "SELECT * FROM " + DataBases.CreateDB._TABLENAME;

        mDB = mDBHelper.getWritableDatabase();
        Cursor cursor = mDB.rawQuery(selectAllQuery, null);

        if(cursor.moveToFirst()){
            do {
                FriendUser data = new FriendUser(
                        cursor.getString(cursor.getColumnIndex(DataBases.CreateDB.USER_ID)),
                        cursor.getString(cursor.getColumnIndex(DataBases.CreateDB.NAME)),
                        cursor.getString(cursor.getColumnIndex(DataBases.CreateDB.THUMBNAIL)));
                friendList.add(data);
            } while (cursor.moveToNext());
        }

        return friendList;
    }


    public String getAllFriendId() {
        String friendIds = "";
        String query = "SELECT " + DataBases.CreateDB.USER_ID + " FROM " + DataBases.CreateDB._TABLENAME;
        mDB = mDBHelper.getWritableDatabase();
        Cursor cursor = mDB.rawQuery(query, null);
        if(cursor.moveToFirst()){
            do {
                friendIds += (cursor.getString(0) + ",");
            } while (cursor.moveToNext());
        }

        return friendIds;
    }

    public FriendUser getFriend (String userId){
        mDB = mDBHelper.getWritableDatabase();

        Cursor cursor = mDB.query(DataBases.CreateDB._TABLENAME, new String[] {DataBases.CreateDB.USER_ID, DataBases.CreateDB.NAME, DataBases.CreateDB.THUMBNAIL},
                DataBases.CreateDB.USER_ID + "=?", new String[]{String.valueOf(userId)}, null, null, null, null);
        if(cursor != null)
            cursor.moveToFirst();
        FriendUser data = new FriendUser(cursor.getString(cursor.getColumnIndex(DataBases.CreateDB.USER_ID)),
                                         cursor.getString(cursor.getColumnIndex(DataBases.CreateDB.NAME)),
                                         cursor.getString(cursor.getColumnIndex(DataBases.CreateDB.THUMBNAIL)));
        return data;
    }

    public void close(){
        mDB.close();
    }
}
