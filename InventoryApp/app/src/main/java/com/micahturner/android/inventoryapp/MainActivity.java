package com.micahturner.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;

import com.micahturner.android.inventoryapp.data.ProductContract.ProductEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Product loader number
    private static final int PRODUCT_LOADER = 0;

    //Adapter for ListView
    ProductCursorAdapter mCursorAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditProductsActivity.class);
                startActivity(intent);
            }
        });

        //Find the ListView for data
        ListView productListView = findViewById(R.id.main_list);

        //Find the empty_view on the ListView, set the productListView on the emptyView
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        //Setup an Adapter to create a list item for each row of product data in the Cursor
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        //Start the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);

        //Setup item click listener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                //Create new intent to go to EditProductsActivity
                Intent intent = new Intent(MainActivity.this, EditProductsActivity.class);

                //URI: "content://com.micahturner.android.inventoryapp/products/#"
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                //Set the URI on the data field of the intent
                intent.setData(currentProductUri);

                //Log for debugging purposes
                Log.e("INTENT", "Intent created on ID : " + id + " URI " + currentProductUri);

                //Launch the EditProductsActivity to display the data for the current product
                startActivity(intent);
            }
        });
    }



    /**
     * Method to insert hardcoded product data into the database. For debugging purposes only.
     */
    private void insertProduct() {

        //Create a ContentValues object where column names are the keys,
        //and product attributes are the values.
        double priceValue = 3.50;
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "Product");
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceValue);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, 3);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, "SuppliersRus");
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NUMBER, "5551234567");

        //Pass the URI to our Log
        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
        Log.v("NewRowLog: ", "Returned URI: " + newUri);
    }

    /** Helper method to delete all products in the database */
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
        Log.v("FullCatalogActivity", rowsDeleted + " rows deleted from the product database");
    }

    /** Create method for OptionsMenu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**On click method for OptionsMenu */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //User clicked on a menu option
        switch (item.getItemId()) {

            //Click on "Example Data"
            case R.id.dummy_data:
                insertProduct();
                return true;

            //Click on "Delete Database" menu option
            case R.id.delete_data:
                deleteAllProducts();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**Loader for the cursor including projection and creation */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define a projection that specifies the attribute columns from the table we care about
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER_NUMBER};

        //This loader executes the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                ProductEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    /**Loader is finished and swaps cursor data */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Update ProductCursorAdapter with cursor containing updated product data
        mCursorAdapter.swapCursor(data);
    }

    /**Loader is reset and gets new cursor*/
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Callback called when data deleted
        mCursorAdapter.swapCursor(null);
    }
}
