package com.example.marc.jobhelper.Model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.ContactsContract;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Stellt den Zugriffspunkt in die Datenbank für persistente Datenspeicherung dar.
 * Erstellt von Marc am 17.06.17.
 */

public class DatabaseConnection extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "JobHelperDB";
    private static final String TABLE_NAME = "Companies";
    private static final int DATABASE_VERSION = 3;

    private static final String KEY_ID = "id";
    private static final String KEY_BLOB = "blob";
    public static final int DEFAULT_ID = -1;

    private static final String[] COLUMNS = {KEY_ID, KEY_BLOB};

    private static DatabaseConnection connection;

    public static DatabaseConnection getInstance(Context context){
        if(connection == null) connection = new DatabaseConnection(context);
        return connection;
    }

    private DatabaseConnection(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String DeleteCompanyTable = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(DeleteCompanyTable);
        String CreateCompanyTable = "CREATE TABLE " + TABLE_NAME + " ( "
                + KEY_ID + " INTEGER PRIMARY KEY, "
                + KEY_BLOB + " BLOB "
                + ")";
        db.execSQL(CreateCompanyTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String DeleteCompanyTable = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(DeleteCompanyTable);
        this.onCreate(db);
    }


    public Company loadCompany(int index){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if(index == DEFAULT_ID) return null;
        try {
            cursor = db.query(TABLE_NAME, COLUMNS, " " + KEY_ID + " = ?", new String[]{String.valueOf(index)}, null, null, null, null);
        }
        catch(android.database.CursorIndexOutOfBoundsException ex){}

        if(cursor != null)
            if(!cursor.moveToFirst()) return null;
        byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_BLOB));
        byte[] cutBlob = Arrays.copyOfRange(blob, 0,  blob.length-1);
        String json = new String(cutBlob);
        Gson gson = new Gson();
        cursor.close();
        return gson.fromJson(json, new TypeToken<Company>() {}.getType());
    }
    /**
     * Alle Companies komplett aus der Datenbank holen, um hohe Reaktionsgeschwindigeit zu erhalten.
     * @return Liste die alle Objekte vom Typ Company beinhaltet.
     */

    public List<Company> loadAllCompaines(){
        SQLiteDatabase db = this.getReadableDatabase();
        List<Company> companies = new ArrayList<>();
        Company tempCompany;
        Cursor cursor = db.query(TABLE_NAME, COLUMNS, null, null, null, null, null, null);
        System.out.println("cursor.moveToFirst: " + cursor.moveToFirst());
        System.out.println("cursor.moveToNext: " + cursor.moveToNext());
        if(cursor != null){
            if(!cursor.moveToFirst()) return null;
            do{
                byte[] blob = cursor.getBlob(cursor.getColumnIndex(KEY_BLOB));
                byte[] cutBlob = Arrays.copyOfRange(blob, 0,  blob.length-1);
                String json = new String(cutBlob);
                System.out.println("JSON beim Laden aus DB: " + json);
                Gson gson = new Gson();
                tempCompany = gson.fromJson(json, new TypeToken<Company>(){}.getType()); //FIXME
                companies.add(tempCompany);
            }while(cursor.moveToNext());
        }
        //TODO Alle Companies in die ArrayList laden.

        db.close();
        return companies;

    }

    public void saveCompanies(List<Company> companies){
        SQLiteDatabase db = this.getWritableDatabase();
        Gson gson = new Gson();
        ContentValues values = new ContentValues();
        for (Company tempCompany : companies) {
            values.put(KEY_ID, tempCompany.getIndex());
            values.put(KEY_BLOB, gson.toJson(tempCompany).getBytes());
        }

        db.insert(TABLE_NAME, null, values);
        db.close();

    }

    public void addCompany(Company company) {
        SQLiteDatabase db = this.getReadableDatabase();
        Gson gson = new Gson();
        ContentValues values = new ContentValues();
        System.out.println("JSON beim Spichern in DB: " + gson.toJson(company));
        values.put(KEY_ID, company.getIndex());
        values.put(KEY_BLOB, gson.toJson(company));
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void removeCompanyAtIndex(int index){
        SQLiteDatabase db = this.getWritableDatabase();
        System.out.println("Deleting Item Index No. " + index);
        db.delete(TABLE_NAME, KEY_ID + " = ?", new String[]{String.valueOf(index)});
        db.close();
    }
}




