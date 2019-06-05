package com.example.apcp;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//DB관리
public class DBHelper extends SQLiteOpenHelper {
    public static int DATABASE_VERSION = 2;

    public DBHelper(Context context) {
        super(context, "senserDB.db", null, DATABASE_VERSION);
    }

    //Table create
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql="create table tb_senser ("+
        "_id integer primary key autoincrement,"+
        "tem," +
        "wet," +
        "dust,"+
        "co)";
        db.execSQL(sql);

        String btSql = "create table devicename ( name text primary key )";
        db.execSQL(btSql);

    }

    //스키마 변경
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion == DATABASE_VERSION){
            String btSql = "create table devicename ( name text primary key )";
            db.execSQL(btSql);
        }
    }

    public void saveDeviceName(String device) {
        Log.e("DBHelper", "saveDeviceName");
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("INSERT OR REPLACE INTO devicename ( name ) VALUES ( '" + device + "' );");

    }

    public String getDeviceName() {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM devicename", null);
        cursor.moveToFirst();
        String result = "";
        if(cursor.moveToPosition(0))result = cursor.getString(0);

        cursor.close();

        Log.e("DBHelper", "" + result);
        return result;
    }
}
