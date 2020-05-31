package com.cloudiera.collegeconexion.Utils.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "CC_User_db";
    public static final String TABLE_NAME = "users";
    public static final String USERS_COLUMN_ID = "id";

    public static final String USERS_COLUMN_BIO = "bio";
    public static final String USERS_COLUMN_BRANCH = "branch";
    public static final String USERS_COLUMN_NAME = "name";
    public static final String USERS_COLUMN_COLLEGE_ID = "college_id";
    public static final String USERS_COLUMN_DOB = "dob";
    public static final String USERS_COLUMN_GENDER = "gender";
    public static final String USERS_COLUMN_EMAIL = "email";
    public static final String USERS_COLUMN_PASS = "password";
    public static final String USERS_COLUMN_PHONE = "phone";
    public static final String USERS_COLUMN_IMAGE = "image";
    public static final String USERS_COLUMN_IMAGE_THUMB = "image_thumb";
    public static final String USERS_COLUMN_ROLL_NO = "roll_no";
    public static final String USERS_COLUMN_COURSE = "course";
    public static final String USERS_COLUMN_VERIFICATION_ID = "verification_id";


    public UserDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME , null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE TABLE_NAME " + "(USERS_COLUMN_ID integer PRIMARY KEY," +
                        "USERS_COLUMN_BIO text, " +
                        "USERS_COLUMN_BRANCH text," +
                        "USERS_COLUMN_NAME text," +
                        "USERS_COLUMN_COLLEGE_ID text," +
                        "USERS_COLUMN_DOB text," +
                        "USERS_COLUMN_GENDER text," +
                        "USERS_COLUMN_EMAIL text," +
                        "USERS_COLUMN_PASS text," +
                        "USERS_COLUMN_PHONE text," +
                        "USERS_COLUMN_IMAGE text," +
                        "USERS_COLUMN_IMAGE_THUMB text," +
                        "USERS_COLUMN_ROLL_NO text," +
                        "USERS_COLUMN_COURSE text," +
                        "USERS_COLUMN_VERIFICATION_ID text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user");
        onCreate(db);
    }

    public void insertContact(String id,
                              String bio,
                              String branch,
                              String name,
                              String college_id,
                              String dob,
                              String gender,
                              String email,
                              String password,
                              String phone,
                              String image,
                              String imageThumb,
                              String rollNo,
                              String course,
                              String verification_id) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(USERS_COLUMN_ID, id);
        contentValues.put(USERS_COLUMN_BIO, bio);
        contentValues.put(USERS_COLUMN_BRANCH, branch);
        contentValues.put(USERS_COLUMN_NAME, name);
        contentValues.put(USERS_COLUMN_COLLEGE_ID, college_id);
        contentValues.put(USERS_COLUMN_DOB, dob);
        contentValues.put(USERS_COLUMN_GENDER, gender);
        contentValues.put(USERS_COLUMN_EMAIL, email);
        contentValues.put(USERS_COLUMN_PASS, password);
        contentValues.put(USERS_COLUMN_PHONE, phone);
        contentValues.put(USERS_COLUMN_IMAGE, image);
        contentValues.put(USERS_COLUMN_IMAGE_THUMB, imageThumb);
        contentValues.put(USERS_COLUMN_ROLL_NO, rollNo);
        contentValues.put(USERS_COLUMN_COURSE, course);
        contentValues.put(USERS_COLUMN_VERIFICATION_ID, verification_id);

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

