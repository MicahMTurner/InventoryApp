package com.micahturner.android.inventoryapp;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.micahturner.android.inventoryapp.data.ProductContract.ProductEntry;

import java.text.NumberFormat;

/**
 * ProductCursorAdapter is an adapter for a list or grid view
 * that uses a Cursor of product data as its data source
 */
public class ProductCursorAdapter extends CursorAdapter {
    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }
    /**
     * Makes a new blank list item view. No data is set to the views yet
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        //Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }
    /**
     * This method binds the product data to the given list item layout.
     * For example, the name for the current product can be set on the listItem_textView_name
     * TextView in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        //Get views from list_item.xml
        TextView textViewName = view.findViewById(R.id.listItem_textView_name);
        TextView textViewPrice = view.findViewById(R.id.listItem_textView_price);
        TextView textViewQuantity = view.findViewById(R.id.listItem_textView_quantity);
        Button buttonSale = view.findViewById(R.id.listItem_button_sale);

        //Insert info from cursor object
        int nameColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityCol = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);

        //Include info not on the list but needed in the next activity
        final int supplierNameColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
        final int supplierPhoneColumn = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NUMBER);
        final int idCol = cursor.getColumnIndex(ProductEntry._ID);

        //Format variables based on output values from cursor
        final String name = cursor.getString(nameColumn);
        final Double price = cursor.getDouble(priceColumn);
        final int quantity = cursor.getInt(quantityCol);
        final String supplierPhoneNumber = cursor.getString(supplierPhoneColumn);
        final String supplierName = cursor.getString(supplierNameColumn);
        final int id = cursor.getInt(idCol);

        //Format the price
        NumberFormat format = NumberFormat.getCurrencyInstance();
        String finalPrice = "$0.00";
        if (price != 0) {
            finalPrice = format.format(price);
        }
        String finalQuantity = context.getString(R.string.adapter_quantity) + Integer.toString(quantity);

        //Set the data to the list
        textViewName.setText(name);
        textViewQuantity.setText(finalQuantity);
        textViewPrice.setText(finalPrice);

        //Set a listener on the sale button
        buttonSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0) {

                    //Build the Uri
                    Uri currentUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                    //Build the content values
                    int newQuantity = quantity - 1;
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_NAME, name);
                    values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER_NUMBER, supplierPhoneNumber);
                    values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierName);
                    values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);

                    //Attempt update
                    int result = context.getContentResolver().update(currentUri, values, null, null);
                    if (result == 0) {
                        //Fail
                        Toast.makeText(context, context.getString(R.string.toast_fail), Toast.LENGTH_SHORT).show();
                    } else {
                        //Success
                        Toast.makeText(context, context.getString(R.string.toast_success), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //Nothing to sell
                    Toast.makeText(context, R.string.toast_nothing, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
