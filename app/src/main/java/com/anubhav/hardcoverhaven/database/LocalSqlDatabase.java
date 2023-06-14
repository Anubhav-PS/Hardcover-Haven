package com.anubhav.hardcoverhaven.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.anubhav.hardcoverhaven.enums.NotifyStatus;
import com.anubhav.hardcoverhaven.interfaces.iOnNotifyDbProcess;
import com.anubhav.hardcoverhaven.models.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LocalSqlDatabase extends SQLiteOpenHelper {


    private static final String USER_TABLE = "USER_TABLE";

    private static final String USER_UID = "USER_UID";
    private static final String APPLICATION_ID = "APPLICATION_ID";
    private static final String STUDENT_MAIL_ID = "STUDENT_MAIL_ID";
    private static final String STUDENT_REGISTER_NUMBER = "STUDENT_REGISTER_NUMBER";
    private static final String STUDENT_NAME = "STUDENT_NAME";
    private static final String STUDENT_CONTACT_NUMBER = "STUDENT_CONTACT_NUMBER";
    private static final String FCM_TOKEN = "FCM_TOKEN";

    private static final int DB_Version = 1;
    private static ExecutorService executors;
    private final int N = 2;
    iOnNotifyDbProcess notify;


    //constructors
    public LocalSqlDatabase(@Nullable Context context) {
        super(context, "HARDCOVER_HAVEN_DB", null, DB_Version);
        assert context != null;
    }

    public LocalSqlDatabase(@Nullable Context context, iOnNotifyDbProcess notify) {
        super(context, "HARDCOVER_HAVEN_DB", null, DB_Version);
        this.notify = notify;
        executors = Executors.newFixedThreadPool(N);
    }

    //executor methods
    public static ExecutorService getExecutors() {
        return executors;
    }

    public static void stopExecutors() {
        executors.shutdown();
    }

    // on create methods
    @Override
    public void onCreate(SQLiteDatabase db) {
        createUserTable(db);
    }

    private void createUserTable(SQLiteDatabase db) {
        final String createTableStatement
                = "CREATE TABLE " + USER_TABLE +
                " (" +
                USER_UID + " TEXT PRIMARY KEY , " +
                APPLICATION_ID + " TEXT , " +
                STUDENT_MAIL_ID + " TEXT , " +
                STUDENT_REGISTER_NUMBER + " TEXT , " +
                STUDENT_NAME + " TEXT , " +
                STUDENT_CONTACT_NUMBER + " TEXT , " +
                FCM_TOKEN + " TEXT )";

        db.execSQL(createTableStatement);

    }

    //upgrade table method
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE);
        onCreate(db);
    }

    //update users

    //user methods
    //insert users
    public boolean addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(USER_UID, user.getUser_UID());
        cv.put(APPLICATION_ID, user.getApplicationID());
        cv.put(STUDENT_MAIL_ID, user.getStudentMailId());
        cv.put(STUDENT_REGISTER_NUMBER, user.getStudentRegisterNumber());
        cv.put(STUDENT_NAME, user.getStudentName());
        cv.put(STUDENT_CONTACT_NUMBER, user.getStudentContactNumber());
        cv.put(FCM_TOKEN, user.getFcmToken());

        long result = db.insert(USER_TABLE, null, cv);
        db.close();
        return result != -1;
    }


    //read users
    public User getCurrentUser() {

        User user = User.getInstance();
        final String query = "SELECT * FROM " + USER_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            user.setUser_UID(cursor.getString(0));
            user.setApplicationID(cursor.getString(1));
            user.setStudentMailId(cursor.getString(2));
            user.setStudentRegisterNumber(cursor.getString(3));
            user.setStudentName(cursor.getString(4));
            user.setStudentContactNumber(cursor.getString(5));
            user.setFcmToken(cursor.getString(6));
        }

        cursor.close();
        db.close();
        return user;

    }

    public void updateUserInBackground(User user) {
        Future<String> result = executors.submit(runParallelUserUpdate(user), "Done");
        executors.submit(hasTaskCompleted(result, NotifyStatus.USER_UPDATED));
    }

    public void addUserInBackground(User user) {
        Future<String> result = executors.submit(runParallelUserInsertion(user), "Done");
        executors.submit(hasTaskCompleted(result, NotifyStatus.USER_ADDED));
    }

    public Runnable runParallelUserInsertion(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        return () -> {
            ContentValues cv = new ContentValues();

            cv.put(USER_UID, user.getUser_UID());
            cv.put(APPLICATION_ID, user.getApplicationID());
            cv.put(STUDENT_MAIL_ID, user.getStudentMailId());
            cv.put(STUDENT_REGISTER_NUMBER, user.getStudentRegisterNumber());
            cv.put(STUDENT_NAME, user.getStudentName());
            cv.put(STUDENT_CONTACT_NUMBER, user.getStudentContactNumber());
            cv.put(FCM_TOKEN, user.getFcmToken());

            long result = db.insert(USER_TABLE, null, cv);
            db.close();
            System.out.println("inserted data successfully");
        };
    }

    private Runnable runParallelUserUpdate(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        return () -> {
            ContentValues cv = new ContentValues();

            cv.put(APPLICATION_ID, user.getApplicationID());
            cv.put(STUDENT_REGISTER_NUMBER, user.getStudentRegisterNumber());
            cv.put(STUDENT_NAME, user.getStudentName());
            cv.put(STUDENT_CONTACT_NUMBER, user.getStudentContactNumber());
            cv.put(FCM_TOKEN, user.getFcmToken());

            db.update(USER_TABLE, cv, "USER_UID = ?", new String[]{user.getUser_UID()});
            db.close();
        };
    }

    //delete tables
    public boolean deleteCurrentUser() {
        final String query = "delete from " + USER_TABLE;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            db.close();
            return true;
        }
        cursor.close();
        db.close();
        return false;
    }


    private Runnable hasTaskCompleted(Future<String> result, NotifyStatus notifyStatus) {
        return () -> {
            while (!result.isDone()) {

            }
            if (notifyStatus == NotifyStatus.USER_UPDATED) {
                notify.notifyUserUpdated();
            } else if (notifyStatus == NotifyStatus.USER_ADDED) {
                System.out.println("in the completed - notify stage");
                notify.notifyUserAdded();
            }
        };
    }


}