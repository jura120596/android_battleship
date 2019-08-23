package com.yura.buttleship;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    final static String CREATE_TABLE = "CREATE TABLE records(username text, time real)";
    final static String DB_NAME = "battleship.db";
    Context mContext;

    public DBHelper(Context context){
        super(context, DB_NAME, null,1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //проверяете какая версия сейчас и делаете апдейт
        db.execSQL("DROP TABLE IF EXISTS records");
        onCreate(db);
    }
}