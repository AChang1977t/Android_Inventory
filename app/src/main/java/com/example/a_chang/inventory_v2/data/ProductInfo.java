package com.example.a_chang.inventory_v2.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class ProductInfo {

    private ProductInfo() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.a_chang.inventory_v2";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRODUCTS = "Product_Inventory";

    public static final class ProductEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public final static String TABLE_NAME = "Product_Inventory";

        public final static String _ID = BaseColumns._ID;

        public final static String COLUMN_PRODUCT_NAME = "Product";

        public final static String COLUMN_SUPPLIER = "Supplier";

        public final static String COLUMN_PRICE = "Price";

        public final static String COLUMN_QUANTITY = "Quantity";

        public final static String COLUMN_IMAGE = "Photo";
    }
}
