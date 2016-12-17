package com.markopaivarinta.hyperg1ant;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.google.android.gms.vision.text.Text;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Map;

public class ItemViewActivity extends AppCompatActivity  {
    final String TAG = "HyperG1ant_CouchbaseEvents_ITEM_VIEW";
    Database database;
    Manager manager;
    Document document;
    String dbname = "hyperg1ant";
    Item item;
    private Context mContext;

    ImageView itemImage;
    TextView textViewTitle;
    TextView textViewPrice;
    TextView textViewContact;
    TextView textViewDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_view);

        Log.d(TAG, "Begin Couchbase HyperG1ant ITEM VIEW");
        // Setting Database Manager
        setDatabaseManager();

        // Retrive ID of Selected Item
        Bundle bundle = getIntent().getExtras();
        String itemId = bundle.getString("itemId");

        try {
            Item item = new Item();
            Document retrievedDocument = database.getExistingDocument(itemId);
            Map<String, Object> properties = retrievedDocument.getProperties();
            String image = (String) properties.get("imageURL");
            item.title = (String) properties.get("title");
            item.price = (Integer) properties.get("price");
            item.contactInfo = (String) properties.get("contactInfo");
            item.description = (String) properties.get("description");


            Log.d(TAG, "retrievedDocument="+ properties.toString());
            itemImage = (ImageView) findViewById(R.id.itemViewImage);
            textViewTitle = (TextView)findViewById(R.id.tvTitle);
            textViewPrice = (TextView)findViewById(R.id.tvPrice);
            textViewContact = (TextView)findViewById(R.id.tvContact);
            textViewDescription = (TextView)findViewById(R.id.tvDescription);

            Picasso.with(mContext).load(image).resize(1000,1200).into(itemImage);
            textViewTitle.setText(item.title);
            textViewPrice.setText(Html.fromHtml("&#8364;"+String.valueOf(item.getPrice())));
            textViewContact.setText(item.contactInfo);
            textViewDescription.setText(item.description);

            Log.d(TAG, itemId);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "END Couchbase HyperG1ant ITEM VIEW");
    }


    public void setDatabaseManager(){
        try {
            manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
            android.util.Log.d(TAG, "Manager created");
        } catch (IOException e) {
            android.util.Log.e(TAG, "Cannot create manager object");
        }
        try {
            database = manager.getDatabase(dbname);
        } catch (CouchbaseLiteException e) {
        }
        try {
            database = manager.getDatabase(dbname);
            android.util.Log.d(TAG, "Database created");
        } catch (CouchbaseLiteException e) {
            android.util.Log.e(TAG, "Cannot get database");
            return;
        }
    }
}
