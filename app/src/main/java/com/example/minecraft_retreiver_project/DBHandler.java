package com.example.minecraft_retreiver_project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {
    //change version when upgraded
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "minecraft.db";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =  "CREATE TABLE " + DBContract.FormUsers.TABLE_NAME + " (" +
                DBContract.FormUsers._USERID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DBContract.FormUsers.COLUMN_EMAIL + " TEXT," +
                DBContract.FormUsers.COLUMN_PASSWD+ " TEXT," +
                DBContract.FormUsers.COLUMN_PSEUDO + " TEXT)";
        db.execSQL(query);

        String query2 =  "CREATE TABLE " + DBContract.FormServers.TABLE_NAME + " (" +
                DBContract.FormServers._SERVERID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DBContract.FormServers.COLUMN_NAME + " TEXT," +
                DBContract.FormServers.COLUMN_IP+ " TEXT," +
                DBContract.FormServers.COLUMN_MOTD + " TEXT)";
        db.execSQL(query2);

        String query3 = "CREATE TABLE " + DBContract.UserServerRelation.TABLE_NAME + " (" +
                DBContract.UserServerRelation._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                DBContract.UserServerRelation.COLUMN_USER_ID + " BIGINT," +
                DBContract.UserServerRelation.COLUMN_SERVER_ID + " BIGINT," +
                "FOREIGN KEY(" + DBContract.UserServerRelation.COLUMN_USER_ID + ") REFERENCES " +
                DBContract.FormUsers.TABLE_NAME + "(" + DBContract.FormUsers._USERID + ")," +
                "FOREIGN KEY(" + DBContract.UserServerRelation.COLUMN_SERVER_ID + ") REFERENCES " +
                DBContract.FormServers.TABLE_NAME + "(" + DBContract.FormServers._SERVERID + "))";
        db.execSQL(query3);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Suppression de toutes les tables
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.UserServerRelation.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.FormUsers.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DBContract.FormServers.TABLE_NAME);
        // Recréation de la base de données
        onCreate(db);
    }

    public boolean registerUser(String email, String plainPassword, String pseudo) {
        // Chiffrer le mot de passe
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBContract.FormUsers.COLUMN_EMAIL, email);
        values.put(DBContract.FormUsers.COLUMN_PASSWD, hashedPassword); // Stocker le hash
        values.put(DBContract.FormUsers.COLUMN_PSEUDO, pseudo);

        long result = db.insert(DBContract.FormUsers.TABLE_NAME, null, values);
        return result != -1;
    }

    public boolean checkUser(String email, String plainPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {DBContract.FormUsers.COLUMN_PASSWD};
        String selection = DBContract.FormUsers.COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(
                DBContract.FormUsers.TABLE_NAME,
                columns,
                selection,
                selectionArgs,
                null, null, null);

        if (cursor.moveToFirst()) {
            String hashedPassword = cursor.getString(0);
            Log.d("HASHED PASSWD", hashedPassword);
            Log.d("JE PASSE ICI", "JE PASSE ICI ENFT");
            return BCrypt.checkpw(plainPassword, hashedPassword);
        }
        cursor.close();

        return false;
    }


    public void deleteFormID (int id){
        SQLiteDatabase db = this.getWritableDatabase();
     }


}

