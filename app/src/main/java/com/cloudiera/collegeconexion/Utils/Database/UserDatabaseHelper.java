package com.cloudiera.collegeconexion.Utils.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "CC_User_db";
    public static final String CONTACTS_TABLE_NAME = "user";
    public static final String CONTACTS_COLUMN_ID = "id";
    public static final String CONTACTS_COLUMN_USERNAME = "username";
    public static final String CONTACTS_COLUMN_FNAME = "fname";
    public static final String CONTACTS_COLUMN_LNAME = "lname";
    public static final String CONTACTS_COLUMN_EMAIL = "email";
    public static final String CONTACTS_COLUMN_IMAGE = "image";
    public static final String CONTACTS_COLUMN_PASS = "password";
    public static final String CONTACTS_COLUMN_COLLEGE_ID = "college_id";
    public static final String CONTACTS_COLUMN_BIO = "bio";

    public UserDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE user " + "(id integer PRIMARY KEY,username text, fname text, " +
                        "lname text,email text,password text,image text,college_id text,bio text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user");
        onCreate(db);
    }

    public void insertContact(String username, String fname,String lname, String email, String image,
                              String password, String college_id, String bio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("username", username);
        contentValues.put("fname", fname);
        contentValues.put("lname", lname);
        contentValues.put("email", email);
        contentValues.put("password", password);
        contentValues.put("image", image);
        contentValues.put("college_id", college_id);
        contentValues.put("bio", bio);
        db.insert("user", null, contentValues);
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery( "select * from user where id="+id+"", null );
    }

    public Integer deleteContact (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("user",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }
}

