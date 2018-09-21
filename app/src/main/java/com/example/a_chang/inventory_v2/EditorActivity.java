package com.example.a_chang.inventory_v2;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a_chang.inventory_v2.data.ProductInfo;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;

    private Uri mCurrentProductUri;

    private EditText mProductName;
    private EditText mSupplier;
    private TextView mQuantity;
    private EditText mPrice;
    private ImageView mImageView;
    private Button mInsert;

    private static int GALLERY = 1, CAMERA = 2;
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.a_chang.inventory_v2.myfileprovider";
    private Uri mImageUri;
    private boolean isGalleryPicture = false;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final String CAMERA_DIR = "/dcim/";


    private boolean mProductChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();

        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product
        if (mCurrentProductUri == null) {
            // This is a new Product, so change the app bar to say "add new product"
            setTitle(getString(R.string.editor_activity_title_new_product));

            invalidateOptionsMenu();

        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit product information"
            setTitle(getString(R.string.editor_activity_title_edit_Pet));


            // Initialize a loader to read the product data from database
            // and "display the current values" in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        mProductName = (EditText) findViewById(R.id.edit_product_name);
        mSupplier = (EditText) findViewById(R.id.edit_supplier);
        mQuantity = (TextView) findViewById(R.id.edit_quantity);
        mPrice = (EditText) findViewById(R.id.edit_price);
        mImageView = (ImageView) findViewById(R.id.edit_image);
        mInsert = (Button) findViewById(R.id.insert_photo);

        mProductName.setOnTouchListener(mTouchListener);
        mSupplier.setOnTouchListener(mTouchListener);
        mQuantity.setOnTouchListener(mTouchListener);
        mPrice.setOnTouchListener(mTouchListener);
        mInsert.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);

    }

    public void decrement(View view) {

        int quantity = Integer.parseInt(mQuantity.getText().toString());
        if (quantity == 0) {
            return;
        }
        quantity--;
        mQuantity.setText(String.valueOf(quantity));
    }


    public void increment(View view) {
        int quantity = Integer.parseInt(mQuantity.getText().toString());
        quantity++;
        mQuantity.setText(String.valueOf(quantity));
    }

    public void order(View view) {
        mProductName = (EditText) findViewById(R.id.edit_product_name);
        String productName = mProductName.getText().toString();
        mPrice = (EditText) findViewById(R.id.edit_price);
        String price = mPrice.getText().toString();
        int basePrice = Integer.parseInt(price);
        String quantityString = mQuantity.getText().toString().trim();
        int quantity = Integer.parseInt(quantityString);
        int calculatePrice = basePrice * quantity;

        String orderMessage = createOrderSummary(productName, quantity, price, calculatePrice);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Barchen Shop order for " + productName);
        intent.putExtra(Intent.EXTRA_TEXT, orderMessage);


        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private String createOrderSummary(String productName, int quantity, String price,
                                      int calculatePrice) {
        String orderMessage = "Product Name: " + productName;
        orderMessage += "\nQuantity: " + quantity;
        orderMessage += "\nPrice: " + price + "/unit";
        orderMessage += "\nTotal: " + calculatePrice;
        return orderMessage;
    }

    public void insertPhoto(View view) {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(view.getContext());
        builder.setTitle("how to pick the photo?");
        builder.setPositiveButton("from gallery", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent pickPhoto;

                if (Build.VERSION.SDK_INT < 19) {
                    pickPhoto = new Intent(Intent.ACTION_GET_CONTENT);
                } else {
                    pickPhoto = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    pickPhoto.addCategory(Intent.CATEGORY_OPENABLE);
                }
                pickPhoto.setType("image/*");
                startActivityForResult(Intent.createChooser(pickPhoto, "Select Picture"), 1);
            }
        });

        builder.setNegativeButton("from camera", new DialogInterface.OnClickListener()

        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // Open Camera

                try {
                    File photoFile = createImageFile();

                    mImageUri = FileProvider.getUriForFile(EditorActivity.this,
                            FILE_PROVIDER_AUTHORITY, photoFile);

                    takePhoto.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(takePhoto, PackageManager.MATCH_DEFAULT_ONLY);
                        for (ResolveInfo resolveInfo : resInfoList) {
                            String packageName = resolveInfo.activityInfo.packageName;
                            grantUriPermission(packageName, mImageUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                    }
                    if (takePhoto.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePhoto, 2);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }


            private File createImageFile() throws IOException {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = JPEG_FILE_PREFIX + timeStamp;
                File albumF = getAlbumDir();
                File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
                return imageF;
            }

            private File getAlbumDir() {
                File storageDir = null;
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    storageDir = new File(Environment.getExternalStorageDirectory() + CAMERA_DIR + getString(R.string.app_name));
                    if (storageDir != null) {
                        if (!storageDir.mkdir()) {
                            if (!storageDir.exists()) {
                                return null;
                            }
                        }
                    }
                }
                return storageDir;
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY && resultCode == RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();
                mImageView.setImageBitmap(getBitmapFromUri(mImageUri));
            }

            isGalleryPicture = true;


        } else if (requestCode == CAMERA && resultCode == RESULT_OK) {
            mImageView.setImageBitmap(getBitmapFromUri(mImageUri));

            isGalleryPicture = false;
        }
    }

    private void saveProduct() {
        String productString = mProductName.getText().toString().trim();
        String supplierString = mSupplier.getText().toString().trim();
        String priceString = mPrice.getText().toString().trim();
        String quantityString = mQuantity.getText().toString().trim();
        String imageString = "";
        if (mImageUri != null) {
            imageString = mImageView.toString().trim();
        } else {
            imageString = null;
        }


        if (TextUtils.isEmpty(productString)) {
            Toast.makeText(getApplicationContext(), "No name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(supplierString)) {
            Toast.makeText(getApplicationContext(), "No supplier's name", Toast.LENGTH_SHORT).show();
            return;
        }

        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        } else {
            Toast.makeText(getApplicationContext(), "Price must be filled in.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(imageString)) {
            Toast.makeText(getApplicationContext(), "Image must be inserted", Toast.LENGTH_SHORT).show();
            return;
        }


        // Add below sentence to prevent crash with blank editor
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(productString) && TextUtils.isEmpty(supplierString) &&
                TextUtils.isEmpty(priceString) && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(imageString)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProductInfo.ProductEntry.COLUMN_PRODUCT_NAME, productString);
        values.put(ProductInfo.ProductEntry.COLUMN_SUPPLIER, supplierString);
        values.put(ProductInfo.ProductEntry.COLUMN_PRICE, price);

        int quantity = Integer.parseInt(quantityString);
        if (quantity >= 0) {
            values.put(ProductInfo.ProductEntry.COLUMN_QUANTITY, quantity);
        } else {
            Toast.makeText(getApplicationContext(), "Quantity should be valid value.", Toast.LENGTH_SHORT).show();
        }

        if (mImageUri == null) {
            values.put(ProductInfo.ProductEntry.COLUMN_IMAGE, imageString);
        } else {
            values.put(ProductInfo.ProductEntry.COLUMN_IMAGE, mImageUri.toString());
        }


        mImageView.setImageBitmap(getBitmapFromUri(mImageUri));

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductInfo.ProductEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    private Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null) {
            return null;
        }
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            int photoW = opts.outWidth;
            int photoH = opts.outHeight;

            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            opts.inJustDecodeBounds = false;
            opts.inSampleSize = scaleFactor;
            opts.inPurgeable = true;

            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, opts);

            if (image.getWidth() > image.getHeight()) {
                Matrix mat = new Matrix();
                int degree = 90;
                mat.postRotate(degree);
                Bitmap imageRotate = Bitmap.createBitmap(image, 0, 0, image.getWidth(),
                        image.getHeight(), mat, true);
                return imageRotate;
            } else {
                return image;
            }

        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.home:
                // If product hasn't changed, continue with navigating up to parent activity
                if (!mProductChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // user clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void showDeleteConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
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

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_sucessful), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handing back button press
        if (!mProductChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" Button, navigate to parent activity.
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
        // show a dialog that notifies the user they have unsaved changed.
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changed_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.Keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        //Create and the AlterDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                ProductInfo.ProductEntry._ID,
                ProductInfo.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductInfo.ProductEntry.COLUMN_SUPPLIER,
                ProductInfo.ProductEntry.COLUMN_PRICE,
                ProductInfo.ProductEntry.COLUMN_QUANTITY,
                ProductInfo.ProductEntry.COLUMN_IMAGE,
        };
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int productColumnIndex = cursor.getColumnIndex(ProductInfo.ProductEntry.COLUMN_PRODUCT_NAME);
            int supplierColumnIndex = cursor.getColumnIndex(ProductInfo.ProductEntry.COLUMN_SUPPLIER);
            int priceColumnIndex = cursor.getColumnIndex(ProductInfo.ProductEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductInfo.ProductEntry.COLUMN_QUANTITY);
            int imageColumnIndex = cursor.getColumnIndex(ProductInfo.ProductEntry.COLUMN_IMAGE);

            String product = cursor.getString(productColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String image = cursor.getString(imageColumnIndex);

            mProductName.setText(product);
            mSupplier.setText(supplier);
            mPrice.setText(Integer.toString(price));
            mQuantity.setText(String.valueOf(quantity));

            mImageView.setImageURI(Uri.parse(image));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductName.setText("");
        mSupplier.setText("");
        mPrice.setText("");
        mQuantity.setText("");

    }
}

