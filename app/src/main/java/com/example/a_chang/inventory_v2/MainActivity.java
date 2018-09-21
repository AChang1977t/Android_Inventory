package com.example.a_chang.inventory_v2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.a_chang.inventory_v2.data.ProductDbHelper;
import com.example.a_chang.inventory_v2.data.ProductInfo;

import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;

    ProductCursorAdapter mCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView productListView = (ListView) findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri currentProductUri = ContentUris.withAppendedId(ProductInfo.ProductEntry.CONTENT_URI, id);
                intent.setData(currentProductUri);
                startActivity(intent);

            }
        });

        //Kick off the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    private void insertProduct() {

        ContentValues values = new ContentValues();
        values.put(ProductInfo.ProductEntry.COLUMN_PRODUCT_NAME, "iPad");
        values.put(ProductInfo.ProductEntry.COLUMN_SUPPLIER, "Apple");
        values.put(ProductInfo.ProductEntry.COLUMN_PRICE, "350");
        values.put(ProductInfo.ProductEntry.COLUMN_QUANTITY, "30");
        values.put(ProductInfo.ProductEntry.COLUMN_IMAGE, "default");

        Uri newUri = getContentResolver().insert(ProductInfo.ProductEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            case R.id.action_delete_all_information:
                showDeleteConfirmationDialog();
                // deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_All_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                deleteAllProducts();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductInfo.ProductEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + "rows delted from Product database");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductInfo.ProductEntry._ID,
                ProductInfo.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductInfo.ProductEntry.COLUMN_SUPPLIER,
                ProductInfo.ProductEntry.COLUMN_PRICE,
                ProductInfo.ProductEntry.COLUMN_QUANTITY
        };

        return new CursorLoader(this,
                ProductInfo.ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }
}
