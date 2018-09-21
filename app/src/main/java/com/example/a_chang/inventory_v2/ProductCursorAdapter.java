package com.example.a_chang.inventory_v2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.a_chang.inventory_v2.data.ProductInfo;

import org.w3c.dom.Text;

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        TextView productTextView = (TextView) view.findViewById(R.id.product);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView supplierTextView = (TextView) view.findViewById(R.id.supplier);
        TextView quantityTextView = (TextView) view.findViewById(R.id.currentQuantity);
        Button saleBtn = (Button) view.findViewById(R.id.sale);

        int cursor_id = cursor.getInt(cursor.getColumnIndex(ProductInfo.ProductEntry._ID));
        final Uri currentUri = Uri.withAppendedPath(ProductInfo.ProductEntry.CONTENT_URI, String.valueOf(cursor_id));
        final int idColumnIndex = cursor.getColumnIndex(ProductInfo.ProductEntry._ID);

        int productColumnIndex = cursor.getColumnIndex(ProductInfo.ProductEntry.COLUMN_PRODUCT_NAME);
        int supplierColumnIndex = cursor.getColumnIndex(ProductInfo.ProductEntry.COLUMN_SUPPLIER);
        int priceColumnIndex = cursor.getColumnIndex(ProductInfo.ProductEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductInfo.ProductEntry.COLUMN_QUANTITY);

        int currentQuantity = cursor.getInt(quantityColumnIndex);

        String productName = cursor.getString(productColumnIndex);
        String supplier = cursor.getString(supplierColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        final int productCurrentQuantity = cursor.getInt(quantityColumnIndex);

        if (TextUtils.isEmpty(supplier)) {
            supplier = context.getString(R.string.unknown_supplier);
        }

        productTextView.setText(productName);
        supplierTextView.setText(supplier);
        priceTextView.setText(productPrice);
        quantityTextView.setText(String.valueOf(currentQuantity));

        saleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cursor.moveToPosition(idColumnIndex);
                if (productCurrentQuantity > 0) {
                    ContentValues values = new ContentValues();
                    int currentQuantity = productCurrentQuantity - 1;
                    values.put(ProductInfo.ProductEntry.COLUMN_QUANTITY, currentQuantity);
                    view.getContext().getContentResolver().update(currentUri, values, null, null);
                }
            }
        });


    }


}
