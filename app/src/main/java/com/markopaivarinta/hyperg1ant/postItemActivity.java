package com.markopaivarinta.hyperg1ant;

import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.EditText;
import android.widget.ImageView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class postItemActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    final String TAG = "HyperG1ant_CouchbaseEvents_postItemActivity";
    Button btAddImage;
    Item item;
    EditText tbTitle;
    EditText tbPrice;
    EditText tbContact;
    EditText tbDescription;
    Button btPostItem;
    ImageView thumbNail;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    Manager manager;
    Database database;
    Document document;
    String dbname = "hyperg1ant";

    GoogleApiClient googleApiClient;
    FusedLocationProviderApi fusedLocationProviderApi;
    protected Location currentLocation;
    protected LocationRequest locationRequest;
    protected String mLastUpdateTime;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 5;

    String latitude;
    String longitude;

    private FirebaseStorage mStorage = FirebaseStorage.getInstance();
    StorageReference storageRef = mStorage.getReferenceFromUrl("gs://hyperg1ant-1a512.appspot.com");
    Uri picUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_item);



        btAddImage = (Button) findViewById(R.id.btAddImage);
        btAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }

        });

        thumbNail = (ImageView) findViewById(R.id.ivThumbnail);

        tbTitle = (EditText) findViewById(R.id.tbTitle);
        tbPrice = (EditText) findViewById(R.id.tbPrice);
        tbContact = (EditText) findViewById(R.id.tbEmail);
        tbDescription = (EditText) findViewById(R.id.tbDescription);

        btPostItem = (Button) findViewById(R.id.btSubmit);

        btPostItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("message", "Item posted!");
                Intent intent1 = (new Intent(postItemActivity.this, MainActivity.class).putExtras(bundle));
                startActivity(intent1);
                finish();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Uri uri = data.getData();
            StorageReference filepath = storageRef.child("itemImages");

            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                }
            });


/*
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            thumbNail.setImageBitmap(imageBitmap);
            if (thumbNail != null) {
                btAddImage.setVisibility(View.GONE);
            }
*/



        }
    }


    private void postItem() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Calendar calendar = GregorianCalendar.getInstance();
        String currentTimeString = dateFormatter.format(calendar.getTime());

        String docItemForSaleId = "itemForSale" + ".";

        item = new Item();
        item.title = tbTitle.toString();
        item.price = Integer.parseInt(tbPrice.getText().toString());
        item.contactInfo = tbContact.toString();
        item.description = tbDescription.toString();
        item.itemListedDate = Date.valueOf(currentTimeString);
        item.latitude = latitude;
        item.longitude = longitude;
        item.image = "";

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

        if (!Manager.isValidDatabaseName(dbname)) {
            Log.e(TAG, "Bad database name");
            return;
        }
        // ITEM TO POST
        Map<String, Object> docContent1 = new HashMap<String, Object>();
        docContent1.put("title", item.title);
        docContent1.put("price", item.price);
        docContent1.put("contactInfo", item.contactInfo);
        docContent1.put("description", item.description);
        docContent1.put("itemListedDate", item.itemListedDate);
        docContent1.put("latitude", item.latitude);
        docContent1.put("longitude", item.longitude);
        docContent1.put("imageURL", item.image);
        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent1);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        currentLocation = fusedLocationProviderApi.getLastLocation(googleApiClient);
        if (currentLocation != null) {
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

}