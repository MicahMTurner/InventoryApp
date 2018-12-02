package com.micahturner.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

public class ProductProvider extends ContentProvider {

    //Constant for URI products database
    public static final int PRODUCTS = 100;

    //Constant for URI specific product
    public static final int PRODUCTS_ID = 101;

    //UriMatcher object
    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //Static initializer
    static {
        // This is where the matcher calls to addURI()
        // Domain/Authority + path + id
        mUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS); //All products
        mUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCTS_ID); //One product
    }

    //Database Helper for the products database
    private ProductDbHelper mDBHelper;

    @Override
    public boolean onCreate() {
        //Give the database context
        mDBHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        //Get the database
        SQLiteDatabase db = mDBHelper.getReadableDatabase();

        //Create null cursor
        Cursor cursor = null;

        //URI match with projection
        int match = mUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                //Returns all products
                cursor = db.query(ProductContract.ProductEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case PRODUCTS_ID:
                //Return product by ID
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ProductContract.ProductEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                //Throws illegal argument exception
                throw new IllegalArgumentException("Invalid URI " + uri);
        }
        //Read and return cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        //Returns the mime type
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCTS_ID:
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Uknown URI " + uri);
        }
    }

    //Insert
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertIntoDB(uri, values);
            default:
                throw new IllegalArgumentException("Cannot insert into DB with " + uri);
        }
    }

    private Uri insertIntoDB(Uri uri, ContentValues values) {

        // Check Name, Quantity, Price, Supplier name, Supplier phone1 of which
        //      NONE of these are allowed to be empty.  Quantity can be 0.
        //      I suppose price could be 0 and set later as well.
        Log.d("URI passed", "B4 Check: " + uri);
        checkData(values);

        //Get the database
        SQLiteDatabase db = mDBHelper.getReadableDatabase();

        //Insert into database
        long result = db.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);

        //Check for failure
        if (result == -1) {
            //Failed
            Log.e("Failed INSERT", "URI failed : " + uri);
            return null;
        }

        //Notify resolver
        getContext().getContentResolver().notifyChange(uri, null);

        //Return the URI with an ID
        return ContentUris.withAppendedId(uri, result);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        //Get the database.
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        final int match = mUriMatcher.match(uri);
        int result;
        switch (match) {
            case PRODUCTS:
                //Delete database
                result = db.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                if (result != 0) {
                    //Notify the resolver
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return result;

            case PRODUCTS_ID:
                //Delete the single row by ID
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                result = db.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                if (result != 0) {
                    //Notify the resolver
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return result;

            default:
                throw new IllegalArgumentException("Cannot delete item with uri: " + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        //This is the public method to Update.
        final int match = mUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateItem(uri, values, selection, selectionArgs);
            case PRODUCTS_ID:
                // got an ID ...
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update cannot proceed with uri: " + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //Check the data
        checkData(values);

        //Get the database
        SQLiteDatabase db = mDBHelper.getReadableDatabase();

        //Return the number of rows(elements)
        int result = db.update(ProductContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        //Not null result
        if (result != 0) {
            // Notify of change.
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return result;
    }


    private void checkData(ContentValues values) {
        //Check name
        String name = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("This item requires a NAME");
        }

        //Check quantity is not a negative
        int quantity = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity < 0) {
            throw new IllegalArgumentException("There cannot be a negative QUANTITY");
        }

        //Check Price is greater than zero
        double price = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price < 0) {
            throw new IllegalArgumentException("Price must be at least 0");
        }

        //Check Supplier name is not null
        String supplyName = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER);
        if (supplyName == null) {
            throw new IllegalArgumentException("There must be a supplier name.");
        }

        //Check Supplier phone number
        //Should only contain numbers, as a string, and 10 numbers long
        String supplyNumber = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_NUMBER);
        if (supplyNumber == null) {
            throw new IllegalArgumentException("There must be a supplier number");
        }
        if (supplyNumber.length() != 10) {
            throw new IllegalArgumentException("The phone number must be 10 digits long");
        }
        if (!TextUtils.isDigitsOnly(supplyNumber)) {
            throw new IllegalArgumentException("The phone number must be numbers only.");
        }

    }
}
