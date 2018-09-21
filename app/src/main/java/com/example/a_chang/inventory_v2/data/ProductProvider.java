package com.example.a_chang.inventory_v2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;


public class ProductProvider extends ContentProvider {

    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    private ProductDbHelper mDbHelper;

    private static final int PRODUCTS = 100;

    private static final int PRODUCT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ProductInfo.CONTENT_AUTHORITY, ProductInfo.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductInfo.CONTENT_AUTHORITY, ProductInfo.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:
                cursor = database.query(ProductInfo.ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductInfo.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ProductInfo.ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Can not query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what Content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductInfo.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductInfo.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri + "with match" + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }


    private Uri insertProduct(Uri uri, ContentValues values) {

        String productName = values.getAsString(ProductInfo.ProductEntry.COLUMN_PRODUCT_NAME);
        if (productName == null) {
            throw new IllegalArgumentException("Product must be defined.");
        }

        String supplier = values.getAsString(ProductInfo.ProductEntry.COLUMN_SUPPLIER);
        if (supplier == null) {
            throw new IllegalArgumentException("Supplier must be defined.");
        }

        Integer price = values.getAsInteger(ProductInfo.ProductEntry.COLUMN_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Product must have valid price");
        }

        Integer quantity = values.getAsInteger(ProductInfo.ProductEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Product must have valid quantity");
        }

        String image = values.getAsString(ProductInfo.ProductEntry.COLUMN_IMAGE);
        if (image == null) {
            throw new IllegalArgumentException("Image must be inserted.");
        }


        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(ProductInfo.ProductEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the pet content URI
        // uri: content://com.example.android.pets/pets
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:

                rowsDeleted = database.delete(ProductInfo.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = ProductInfo.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                rowsDeleted = database.delete(ProductInfo.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // return the number of rows deleted
        return rowsDeleted;
    }


    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductInfo.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }


    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.containsKey(ProductInfo.ProductEntry.COLUMN_PRODUCT_NAME)) {
            String productName = values.getAsString(ProductInfo.ProductEntry.COLUMN_PRODUCT_NAME);
            if (productName == null) {
                throw new IllegalArgumentException("Product must define its name");
            }
        }

        if (values.containsKey(ProductInfo.ProductEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(ProductInfo.ProductEntry.COLUMN_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Product must have valid price");
            }
        }

        if (values.containsKey(ProductInfo.ProductEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(ProductInfo.ProductEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Product must have valid quantity");
            }
        }
        // No need to check the supplier and image, any values are valid (including null).
        // If there are no values to update, then don't try to update the database.
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the databae and get the number of rows affected
        int rowsUpdated = database.update(ProductInfo.ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
