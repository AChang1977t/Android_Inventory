package com.example.a_chang.inventory_v2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.a_chang.inventory_v2.MainActivity;

public class ProductDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ProductDbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "product.db";

    private static final int DATABASE_VERSION = 1;


    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_PRODUCT_INVENTORY_TABLE = "CREATE TABLE IF NOT EXISTS " + ProductInfo.ProductEntry.TABLE_NAME + "("
                + ProductInfo.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductInfo.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductInfo.ProductEntry.COLUMN_SUPPLIER + " TEXT, "
                + ProductInfo.ProductEntry.COLUMN_PRICE + " INTEGER NOT NULL, "
                + ProductInfo.ProductEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + ProductInfo.ProductEntry.COLUMN_IMAGE + " TEXT );";
        db.execSQL(SQL_CREATE_PRODUCT_INVENTORY_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int OlderVerstion, int newVersion) {

    }
}
