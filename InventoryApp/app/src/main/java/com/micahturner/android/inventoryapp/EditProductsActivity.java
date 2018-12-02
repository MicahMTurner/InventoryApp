package com.micahturner.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.micahturner.android.inventoryapp.data.ProductContract.ProductEntry;

import java.text.NumberFormat;


public class EditProductsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Loader ID number
    final int LOADER_ID = 0;

    //Current URI
    private Uri mCurrentUri;

    //Fields From URI
    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierEditText;
    private EditText mSupplierPhoneEditText;
    private EditText mPriceEditText;
    private Button mDecrementButton;
    private Button mIncrementButton;
    private Button mDeleteButton;
    private Button mSaveButton;
    private Button mCallButton;

    //Product changed boolean
    private boolean mDataChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {

        //Listens for changes
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mDataChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Get the intent pass the data to the current URI
        Intent intent = getIntent();
        mCurrentUri = intent.getData();

        //Setup the views for activity_edit.xml
        mNameEditText = findViewById(R.id.editor_name); //Product name
        mQuantityEditText = findViewById(R.id.editor_quantity_editText); //Quantity
        mSupplierEditText = findViewById(R.id.editor_supplier_name); //Supplier name
        mSupplierPhoneEditText = findViewById(R.id.editor_supplier_phone); //Supplier phone number
        mPriceEditText = findViewById(R.id.editor_price);  //Price
        mDecrementButton = findViewById(R.id.editor_quantity_buttonMinus); //Decrement button
        mIncrementButton = findViewById(R.id.editor_quantity_buttonPlus); //Increment button
        mDeleteButton = findViewById(R.id.editor_button_deleteCancel); //Delete button
        mSaveButton = findViewById(R.id.editor_button_saveUpdate); //Save button
        mCallButton = findViewById(R.id.editor_button_call); //Call supplier button

        //Set Text for New Item or Edit Item
        if (mCurrentUri == null) {

            //No URI from intent, NEW ITEM.
            setTitle(R.string.editor_label_adding);
            mDeleteButton.setVisibility(View.GONE);
            mSaveButton.setText(getString(R.string.editor_button_save));
            mQuantityEditText.setText("0");
            mCallButton.setVisibility(View.GONE);
        } else {

            //Has URI, get load manager and set buttons
            setTitle(R.string.editor_label);
            getLoaderManager().initLoader(LOADER_ID, null, this);
            mSaveButton.setText(getString(R.string.editor_button_save));
            mDeleteButton.setText(getString(R.string.editor_button_delete));
        }

        //Create the touch listeners
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mDecrementButton.setOnTouchListener(mTouchListener);
        mIncrementButton.setOnTouchListener(mTouchListener);

        //Create the onClickListener for the decrement button
        mDecrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editQuantity = mQuantityEditText.getText().toString().trim();
                if (TextUtils.isEmpty(editQuantity)) {
                    mQuantityEditText.setText("0");
                }
                int currentValue = Integer.valueOf(mQuantityEditText.getText().toString().trim());
                if (currentValue > 0) {
                    currentValue--;
                } else {
                    Toast.makeText(EditProductsActivity.this, "You cannot have negative inventory", Toast.LENGTH_SHORT).show();
                }
                mQuantityEditText.setText(Integer.toString(currentValue));
            }
        });

        //Create the onClickListener for the increment button
        mIncrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String editQuantity = mQuantityEditText.getText().toString().trim();
                if (TextUtils.isEmpty(editQuantity)) {
                    mQuantityEditText.setText("0");
                }
                int currentValue = Integer.valueOf(mQuantityEditText.getText().toString().trim());
                currentValue++;
                mQuantityEditText.setText(Integer.toString(currentValue));
            }
        });

        //Create the onClickListener for the save button
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //On click listener for Save/Delete
                if (mCurrentUri == null) {
                    //New Item save
                    saveItemDialog();
                }
                if (mCurrentUri != null) {
                    //Saving Dialog
                    saveItemDialog();
                }
            }
        });

        //Create the onClickListener for the delete button
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentUri != null) {

                    //Delete Dialog
                    deleteItemDialog();
                }
            }
        });
    }

    //Override Back button 
    public void onBackPressed() {
        if (mDataChanged) {

            //Ask user if they want to save data
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.editor_ask_to_save));
            builder.setPositiveButton(getString(R.string.editor_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (saveItem()) {

                        //Save successful
                        finish();
                    }
                }
            });
            builder.setNegativeButton(getString(R.string.editor_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //Don't save
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    finish();
                }
            });

            //Show dialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else {
            super.onBackPressed();
            return;
        }
    }

    private void deleteItemDialog() {

        //Called to delete an item
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.editor_ask_to_delete));
        builder.setPositiveButton(getString(R.string.editor_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Delete item
                deleteItem();
                finish();
            }
        });

        builder.setNegativeButton(getString(R.string.editor_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Dismiss dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        //Create dialog and show it to the user
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveItemDialog() {

        //Called if the back button is pressed
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.editor_ask_to_save));
        builder.setPositiveButton(getString(R.string.editor_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (saveItem()) {
                    //Save successful
                    finish();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.editor_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Don't save
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        //Create dialog and show it to the user
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * This method validates data from the edit text and
     * saves the information to the database.
     */
    private boolean checkDataBeforeSaving() {

        //Test the name
        if (TextUtils.isEmpty(mNameEditText.getText().toString().trim())) {
            Toast.makeText(this, "There must be a title", Toast.LENGTH_SHORT).show();
            return false;
        }

        //Test the price
        String priceTestStr = mPriceEditText.getText().toString().trim();
        if (TextUtils.isEmpty(priceTestStr)) {
            Toast.makeText(this, "The price is invalid", Toast.LENGTH_SHORT).show();
            return false;
        } else {

            String newPriceTestStr = priceTestStr.replaceAll("[,$]", "");
            Log.e("Str Out: ", "A: " + priceTestStr + "     /      B: " + newPriceTestStr);
            Double priceOut = Double.valueOf(newPriceTestStr);
            if (priceOut < 0) {
                Toast.makeText(this, "The price is invalid", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        //Test the quantity
        if (TextUtils.isEmpty(mQuantityEditText.getText().toString().trim())) {
            Toast.makeText(this, "The quantity is invalid", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            int quantityOut = Integer.valueOf(mQuantityEditText.getText().toString().trim());
            if (quantityOut < 0) {
                Toast.makeText(this, "The quantity is invalid", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        //Test supplier name
        if (TextUtils.isEmpty(mSupplierEditText.getText().toString().trim())) {
            Toast.makeText(this, "The name of the supplier is missing", Toast.LENGTH_SHORT).show();
            return false;
        }

        //Test supplier phone number
        if (TextUtils.isEmpty(mSupplierPhoneEditText.getText().toString().trim())) {
            Toast.makeText(this, "The supplier's phone number is missing", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            String textCheck = mSupplierPhoneEditText.getText().toString().trim();
            if (textCheck.length() != 10) {
                Toast.makeText(this, "Supplier Phone Number must be 10 digits", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    //Delete item
    private void deleteItem() {
        if (mCurrentUri != null) {
            int result = getContentResolver().delete(mCurrentUri, null, null);
            if (result > 0) {
                Toast.makeText(this, getString(R.string.editor_delete_success), Toast.LENGTH_SHORT).show();
                Log.e("DEL_ITEM: ", "  " + result);
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Saves or updates an item
     */
    private boolean saveItem() {
        if (mCurrentUri != null) {

            //Save item(update)
            if (checkDataBeforeSaving()) {

                //Check the data is valid
                String nameOut = mNameEditText.getText().toString().trim();

                //Prevent SQL injection
                String priceConversionString = mPriceEditText.getText().toString().trim();
                String priceConvertedString = priceConversionString.replaceAll("[,$]", "");
                Double priceOut = Double.valueOf(priceConvertedString);
                int quantityOut = Integer.valueOf(mQuantityEditText.getText().toString().trim());
                String supplierNameOut = mSupplierEditText.getText().toString().trim();
                String supplierPhoneOut = mSupplierPhoneEditText.getText().toString().trim();

                //Create content values to insert into the table
                ContentValues values = new ContentValues();

                //Attributes for each row in data base
                values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameOut);
                values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceOut);
                values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityOut);
                values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierNameOut);
                values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NUMBER, supplierPhoneOut);

                //Update data base
                int result = getContentResolver().update(mCurrentUri, values, null, null);
                if (result != 0) {
                    Toast.makeText(EditProductsActivity.this, getString(R.string.editor_save_success), Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    Toast.makeText(EditProductsActivity.this, getString(R.string.editor_save_failed), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        } else {

            //Save new item
            if (checkDataBeforeSaving()) {

                //Check the data is valid
                String nameOut = mNameEditText.getText().toString().trim();

                //Prevent SQL injection
                String priceConversionString = mPriceEditText.getText().toString().trim();
                String priceConvertedString = priceConversionString.replaceAll("[,$]", "");
                Double priceOut = Double.valueOf(priceConvertedString);
                int quantityOut = Integer.valueOf(mQuantityEditText.getText().toString().trim());
                String supplierNameOut = mSupplierEditText.getText().toString().trim();
                String supplierPhoneOut = mSupplierPhoneEditText.getText().toString().trim();

                //Create content values to insert into the table
                ContentValues values = new ContentValues();

                //Attributes for each row in data base
                values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameOut);
                values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceOut);
                values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityOut);
                values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierNameOut);
                values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NUMBER, supplierPhoneOut);

                //Update data base
                Uri result = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
                if (result != null) {
                    Toast.makeText(EditProductsActivity.this, getString(R.string.editor_save_success), Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    Toast.makeText(EditProductsActivity.this, getString(R.string.editor_save_failed), Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        }
        return false;
    }

    //Create the projection
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        
        //Projection for the cursor
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NUMBER,
                ProductEntry.COLUMN_PRODUCT_NAME
        };

        //Cursor returned
        return new CursorLoader(this,
                mCurrentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        //Get values from cursor by attribute(column)
        if (data.moveToFirst()) {
            int nameColumn = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumn = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumn = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumn = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            int supplierPhoneColumn = data.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NUMBER);

            //Format the data for display
            String name = data.getString(nameColumn);
            Double price = data.getDouble(priceColumn);
            int quantity = data.getInt(quantityColumn);
            String supplier = data.getString(supplierColumn);
            final String supplierPhone = data.getString(supplierPhoneColumn);

            // Check price isn't 0
            NumberFormat format = NumberFormat.getCurrencyInstance();
            String outPrice = "0.00";
            if (price != 0) {
                outPrice = format.format(price);
            }

            // Phone must be 10 digits
            String callText = "Call: (" + supplierPhone.substring(0, 3) + ") "
                    + supplierPhone.substring(3, 6) + "-" + supplierPhone.substring(6, 10);

            //OnClickListener to call the supplier using action dial
            mCallButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + supplierPhone));
                    startActivity(intent);
                }
            });

            //Display the data
            mNameEditText.setText(name);
            mPriceEditText.setText(outPrice);
            mQuantityEditText.setText(Integer.toString(quantity));
            mSupplierEditText.setText(supplier);
            mSupplierPhoneEditText.setText(supplierPhone);
            mCallButton.setText(callText);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        //Reset the views
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierEditText.setText("");
        mSupplierPhoneEditText.setText("");
    }
}
