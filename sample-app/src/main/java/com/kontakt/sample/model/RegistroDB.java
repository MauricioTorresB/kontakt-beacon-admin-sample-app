package com.kontakt.sample.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.kontakt.sample.Class.Registro;

public class RegistroDB {
    public static final String keyIdRegistros = "idRegistros";
    public static final String keySim = "sim";
    public static final String keyDeviceId = "deviceId";
    public static final String keyMajor = "major";
    public static final String keyMinor = "minor";
    public static final String keyLatitud = "latitud";
    public static final String keyLongitud = "longitud";
    public static final String keyDato1 = "dato1";
    public static final String keyDato2 = "dato2";
    public static final String keyDato3 = "dato3";

    private static final String DATABASE_NAME = "GenerarEntregas";
    private static final String DATABASE_TABLE = "L_Registros";
    private static final Integer DATABASE_VERSION = 1;

    private static final String DATABASE_DROP = "DROP TABLE IF EXISTS " + DATABASE_TABLE;
    private static final String DATABASE_CREATE =
            " CREATE TABLE IF NOT EXISTS " + DATABASE_TABLE + " ( " +
                    " idRegistros Integer not null primary key autoincrement, " +
                    " sim String not null, " +
                    " deviceId String  not null, " +
                    " major Integer  not null, " +
                    " minor Integer  not null, " +
                    " latitud String  not null, " +
                    " longitud String  not null, " +
                    " dato1 String not null, " +
                    " dato2 String not null, " +
                    " dato3 String not null);";
    //"CREATE INDEX " + DATABASE_TABLE + "_idx ON " + DATABASE_TABLE + " (bulto, codigoArticulo, rutaDetId);COMMIT;";

    private final Context context;
    private RegistroDB.DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public RegistroDB(Context ctx) {
        this.context = ctx;
        DBHelper = new RegistroDB.DatabaseHelper(context);
    }

    public static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onDrop(SQLiteDatabase db) {
            db.execSQL(DATABASE_DROP);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    public void onDrop() throws SQLException {
        DBHelper.onDrop(db);
    }

    public void onCreate() throws SQLException {
        DBHelper.onCreate(db);
    }

    public RegistroDB open() throws SQLException {
        try {
            db = DBHelper.getWritableDatabase();
        } catch (Exception ex) {

        }
        return this;
    }

    public void close() {
        try {
            if (DBHelper != null) {
                DBHelper.close();
            }
        } catch (Exception ex) {
            DBHelper.close();
        }
    }

    public Cursor showList() {
        try {
            String query = "SELECT " +
                    keyIdRegistros +
                    "," + keySim +
                    "," + keyDeviceId +
                    "," + keyMajor +
                    "," + keyMinor +
                    "," + keyLatitud +
                    "," + keyLongitud +
                    "," + keyDato1 +
                    "," + keyDato2 +
                    "," + keyDato3 +
                    " From " + DATABASE_TABLE;
            return db.rawQuery(query, null);
        } catch (Exception ex) {
            String g = ex.getMessage();
            System.out.println();
            return null;
        }
    }

    public long Insert(Registro my) {
        long resultado = 0;

        try {
            ContentValues initialValues = new ContentValues();
            initialValues.put(keySim, my.getSim());
            initialValues.put(keyDeviceId, my.getDeviceId());
            initialValues.put(keyMajor, my.getMajor());
            initialValues.put(keyMinor, my.getMinor());
            initialValues.put(keyLatitud, my.getLatitud());
            initialValues.put(keyLongitud, my.getLongitud());
            initialValues.put(keyDato1, my.getDato1());
            initialValues.put(keyDato2, my.getDato2());
            initialValues.put(keyDato3, my.getDato3());

            resultado = db.insert(DATABASE_TABLE, null, initialValues);
        } catch (Exception ex) {
            String error = ex.getMessage();
            error = error.trim();
        }
        return resultado;
    }

    public boolean Delete(int IdRegistros) {
        String condicion = keyIdRegistros + " = " + IdRegistros;
        return db.delete(DATABASE_TABLE, condicion, null) > 0;
    }

}
