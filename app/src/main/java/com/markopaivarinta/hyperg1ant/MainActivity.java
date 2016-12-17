package com.markopaivarinta.hyperg1ant;


import android.content.Intent;
import android.location.Location;
import android.os.*;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.location.LocationManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Button;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.couchbase.lite.*;
import com.couchbase.lite.android.AndroidContext;
//import com.couchbase.lite.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

//import com.google.android.gms.appdatasearch.DocumentContents;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.*;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    private Button addItem;
    final String TAG = "HyperG1ant_CouchbaseEvents";

    String dbname = "hyperg1ant";

    LocationManager locationManager;
    GoogleApiClient googleApiClient;
    FusedLocationProviderApi fusedLocationProviderApi;
    protected Location currentLocation;
    protected LocationRequest locationRequest;
    protected String mLastUpdateTime;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 5;
    String latitude;
    String longitude;

    List<String> documentIDs;

    String itemLocation(String latitude, String longitude) {
        List<String> currentLocation = new ArrayList<String>();
        currentLocation.add(latitude);
        currentLocation.add(longitude);
        return currentLocation.toString();
    }

    private GridView mGridView;
    //private ProgressBar mProgressBar;

    private GridViewAdapter mGridAdapter;
    private ArrayList<Item> mGridData;
    private String FEED_URL = "http://javatechig.com/?json=get_recent_posts&count=45";

    Database database;
    Manager manager;
    Document document;



    private static final int CAMERA_REQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getLocation();
        Log.d(TAG, "Begin Couchbase HyperG1ant App");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeCouchbase();
        //createTestDatabase();
    }

    private List<String> getAllDocumentID() {
        List<String> documentIDs = new ArrayList<String>();

        Query query = database.createAllDocumentsQuery();
        query.setAllDocsMode(Query.AllDocsMode.ALL_DOCS);
        try {
            QueryEnumerator result = query.run();
            for (Iterator<QueryRow> it = result; it.hasNext(); ) {
                QueryRow row = it.next();
                if (row != null) {
                    documentIDs.add(row.getDocumentId().toString());
                    Log.d(TAG, row.getDocumentId());
                }
            }
        } catch (CouchbaseLiteException e) {

        }
        for (String temp : documentIDs) {
            System.out.println(temp);
        }
        return documentIDs;
    }

    private ArrayList<Item> listOfAllItems() {
        List<String> documentIDs = getAllDocumentID();
        ArrayList<Item> listofAllItems = new ArrayList<Item>();
        try {
            for (String temp : documentIDs) {
                String docID = temp;
                Log.d(TAG, temp);
                Document retrievedDocument = database.getExistingDocument(docID);

                //image retrival
                //Revision rev = retrievedDocument.getCurrentRevision();
                //Attachment att = rev.getAttachment();

                Map<String, Object> properties = retrievedDocument.getProperties();
                String title = (String) properties.get("title");
                int price = (int) properties.get("price");
                Log.d(TAG, price + title);
                Item item = new Item();
                item.title = title;
                item.price = price;
                listofAllItems.add(item);
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not get value");
        }
        return listofAllItems;
    }

    private void initializeCouchbase(){
        try {
            manager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
            Log.d(TAG, "Manager created");
        } catch (IOException e) {
            Log.e(TAG, "Cannot create manager object");
        }
        try {
            database = manager.getDatabase(dbname);
        } catch (CouchbaseLiteException e) {
        }
        try {
            database = manager.getDatabase(dbname);
            Log.d(TAG, "Database created");
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot get database");
            return;
        }
    }

    private void parseResult() {
        Double currentLatitude = currentLocation.getLatitude();
        Double currentLongitude = currentLocation.getLongitude();
        List<String> documentIDs = getAllDocumentID();
        try {
            for (String temp : documentIDs) {


                String docID = temp;

                Document retrievedDocument = database.getExistingDocument(docID);

                Map<String, Object> properties = retrievedDocument.getProperties();

                String title = (String) properties.get("title");
                String image = (String) properties.get("imageURL");
                int price = (int) properties.get("price");
                String itemLatitudeString = (String) properties.get("latitude");
                String itemLongitudeString = (String) properties.get("longitude");

                Double itemLatitude = Double.parseDouble(itemLatitudeString);
                Double itemLongitude = Double.parseDouble(itemLongitudeString);
                Location itemLocation = new Location("");
                itemLocation.setLatitude(itemLatitude);
                itemLocation.setLongitude(itemLongitude);
                String a = String.valueOf(currentLocation.distanceTo(itemLocation));
                Double distanceFromUser1 = Double.parseDouble(a);

                Item item = new Item();
                item.itemId = docID;
                item.title = title;
                item.image = image;
                item.price = price;
                item.distanceFromUser = distanceFromUser1;
                mGridData.add(item);

                Log.d(TAG, "TEST TEST   " +item.distanceFromUser);
            }

            Collections.sort(mGridData, new ItemComparator());
            for (Item tempItem : mGridData) {
                //Log.d(TAG," SORT SORT " +tempItem.title);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void createTestDatabase() {
        String dbname = "hyperg1ant";
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Calendar calendar = GregorianCalendar.getInstance();
        String currentTimeString = dateFormatter.format(calendar.getTime());
        String docItemForSaleId = "itemForSale" + ".";
        if (!Manager.isValidDatabaseName(dbname)) {
            Log.e(TAG, "Bad database name");
            return;
        }
        // Create an object that contains data about the item
        // ITEM 1
        Map<String, Object> docContent1 = new HashMap<String, Object>();
        docContent1.put("title", "Christmas Tree");
        docContent1.put("price", 50);
        docContent1.put("description", "Used Christmas Tree, comes with all ornaments!");
        docContent1.put("itemListedDate", currentTimeString);
        docContent1.put("latitude", "60.1445813");
        docContent1.put("longitude", "25.0508881");
        docContent1.put("contactInfo", "marko@gmail.com");
        docContent1.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/hyperg1ant-1a512.appspot.com/o/itemImages%2Fchristmastree.JPG?alt=media&token=a53656dc-7833-4415-8632-19b00fa514aa");
        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent1);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }

        // ITEM 2
        Map<String, Object> docContent2 = new HashMap<String, Object>();
        docContent2.put("title", "Water Bottle");
        docContent2.put("price", 10);
        docContent2.put("description", "Used Waterbottle, comes washed!");
        docContent2.put("itemListedDate", currentTimeString);
        docContent2.put("latitude", "60.1445813");
        docContent2.put("longitude", "25.0508881");
        docContent2.put("contactInfo", "jim@gmail.com");
        docContent2.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/hyperg1ant-1a512.appspot.com/o/itemImages%2FWater%20Bottle.JPG?alt=media&token=51c6684a-79b0-41ec-992f-1bbf293bd077");
        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent2);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }

        // ITEM 3
        Map<String, Object> docContent3 = new HashMap<String, Object>();
        docContent3.put("title", "Computer Mouse");
        docContent3.put("price", 90);
        docContent3.put("description", " Computer gaming mouse!");
        docContent3.put("itemListedDate", currentTimeString);
        docContent3.put("latitude", "60.1445813");
        docContent3.put("longitude", "25.0508881");
        docContent3.put("contactInfo", "tom@gmail.com");
        docContent3.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/hyperg1ant-1a512.appspot.com/o/itemImages%2Fcomputer%20mouse.JPG?alt=media&token=234172bd-c657-4896-b84c-90519f9ae86c");
        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent3);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }

        // ITEM 4
        Map<String, Object> docContent4 = new HashMap<String, Object>();
        docContent4.put("title", "Adidas Spray Bottle");
        docContent4.put("price", 0);
        docContent4.put("description", "Empty adidas spray bottle");
        docContent4.put("itemListedDate", currentTimeString);
        docContent4.put("latitude", "60.244500");
        docContent4.put("longitude", "24.867998");
        docContent4.put("contactInfo", "dom@gmail.com");
        docContent4.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/hyperg1ant-1a512.appspot.com/o/itemImages%2FAdidas%20Spray.JPG?alt=media&token=38a7fb3c-ba73-49c4-bf4c-458b4549cdc8");
        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent4);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }

        // ITEM 5
        Map<String, Object> docContent5 = new HashMap<String, Object>();
        docContent5.put("title", "Apple Tv");
        docContent5.put("price", 75);
        docContent5.put("description", "Used Apple Tv working condition. HDMI cable included.");
        docContent5.put("itemListedDate", currentTimeString);
        docContent5.put("latitude", "60.1445813");
        docContent5.put("longitude", "25.0508881");
        docContent5.put("contactInfo", "tim@gmail.com");
        docContent5.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/hyperg1ant-1a512.appspot.com/o/itemImages%2FAppleTv.JPG?alt=media&token=2baf52a1-d960-4df2-88c0-6d7253b0fca1");
        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent5);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }

        // ITEM 6
        Map<String, Object> docContent6 = new HashMap<String, Object>();
        docContent6.put("title", "CamelBack BackPack");
        docContent6.put("price", 50);
        docContent6.put("description", "Bla bla bla bla bla");
        docContent6.put("itemListedDate", currentTimeString);
        docContent6.put("latitude", "60.1445813");
        docContent6.put("longitude", "25.0508881");
        docContent6.put("contactInfo", "ken@gmail.com");
        docContent6.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/hyperg1ant-1a512.appspot.com/o/itemImages%2Fbackpack.JPG?alt=media&token=c323b897-c52c-48ca-9df7-fa2cf81739cb");

        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent6);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }

        // ITEM 7
        Map<String, Object> docContent7 = new HashMap<String, Object>();
        docContent7.put("title", "Banana");
        docContent7.put("price", 50);
        docContent7.put("description", "Bla bla bla bla bla");
        docContent7.put("itemListedDate", currentTimeString);
        docContent7.put("latitude", "60.1445813");
        docContent7.put("longitude", "25.0508881");
        docContent7.put("contactInfo", "ken@gmail.com");
        docContent7.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/hyperg1ant-1a512.appspot.com/o/itemImages%2Fbanana.JPG?alt=media&token=1773d23e-5ecc-4d4b-83ce-3eaf1a67b17b");
        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent7);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }

        // ITEM 8
        Map<String, Object> docContent8 = new HashMap<String, Object>();
        docContent8.put("title", "Cute Cat");
        docContent8.put("price", 100000);
        docContent8.put("description", "Bla bla bla bla bla");
        docContent8.put("itemListedDate", currentTimeString);
        docContent8.put("latitude", "60.1445813");
        docContent8.put("longitude", "25.0508881");
        docContent8.put("contactInfo", "ken@gmail.com");
        docContent8.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/hyperg1ant-1a512.appspot.com/o/itemImages%2FCat.JPG?alt=media&token=d92c14de-2c29-44a0-9de4-53d5b60f6219");
        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent8);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }

        // ITEM 9
        Map<String, Object> docContent9 = new HashMap<String, Object>();
        docContent9.put("title", "Black Coffee Maker");
        docContent9.put("price", 15);
        docContent9.put("description", "Bla bla bla bla bla");
        docContent9.put("itemListedDate", currentTimeString);
        docContent9.put("latitude", "60.1445813");
        docContent9.put("longitude", "25.0508881");
        docContent9.put("contactInfo", "ken@gmail.com");
        docContent9.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/hyperg1ant-1a512.appspot.com/o/itemImages%2FCoffeemaker.JPG?alt=media&token=7f17c0d2-aff5-4103-b8a6-18d065892493");

        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent9);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }

        // ITEM 10
        Map<String, Object> docContent10 = new HashMap<String, Object>();
        docContent10.put("title", "Electric Tea Kettle");
        docContent10.put("price", 50);
        docContent10.put("description", "Bla bla bla bla bla");
        docContent10.put("itemListedDate", currentTimeString);
        docContent10.put("latitude", "60.1445813");
        docContent10.put("longitude", "25.0508881");        docContent10.put("contactInfo", "ken@gmail.com");
        docContent10.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/hyperg1ant-1a512.appspot.com/o/itemImages%2Felectrickettle.JPG?alt=media&token=2e31b393-e527-4d9b-881c-1a1590bd8793");

        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent10);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }

        // ITEM 11
        Map<String, Object> docContent11 = new HashMap<String, Object>();
        docContent11.put("title", "Head Phones");
        docContent11.put("price", 120);
        docContent11.put("description", "Bla bla bla bla bla");
        docContent11.put("itemListedDate", currentTimeString);
        docContent11.put("latitude", "60.1445813");
        docContent11.put("longitude", "25.0508881");
        docContent11.put("contactInfo", "ken@gmail.com");
        docContent11.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/hyperg1ant-1a512.appspot.com/o/itemImages%2FHeadphones1.JPG?alt=media&token=bdac91e8-c562-4c3c-b659-499732e74e0b");
        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent11);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }

        // ITEM 12
        Map<String, Object> docContent12 = new HashMap<String, Object>();
        docContent12.put("title", "Lamp");
        docContent12.put("price", 5);
        docContent12.put("description", "Bla bla bla bla bla");
        docContent12.put("itemListedDate", currentTimeString);
        docContent12.put("latitude", "60.1445813");
        docContent12.put("longitude", "25.0508881");
        docContent12.put("contactInfo", "ken@gmail.com");
        docContent12.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/hyperg1ant-1a512.appspot.com/o/itemImages%2Flamp.JPG?alt=media&token=85f37d0b-80e1-42cc-a82f-e4e7665b010c");

        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent12);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }

        // ITEM 13
        Map<String, Object> docContent13 = new HashMap<String, Object>();
        docContent13.put("title", "Orange Pumpkin");
        docContent13.put("price", 10);
        docContent13.put("description", "Bla bla bla bla bla");
        docContent13.put("itemListedDate", currentTimeString);
        docContent13.put("latitude", "60.1445813");
        docContent13.put("longitude", "25.0508881");
        docContent13.put("contactInfo", "ken@gmail.com");
        docContent13.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/hyperg1ant-1a512.appspot.com/o/itemImages%2Fpumpkin.JPG?alt=media&token=26e296ef-5a85-4d56-866b-db5b3e42ac5e");

        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent13);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }

        // ITEM 14
        Map<String, Object> docContent14 = new HashMap<String, Object>();
        docContent14.put("title", "Tv Remote");
        docContent14.put("price", 8);
        docContent14.put("description", "Bla bla bla bla bla");
        docContent14.put("itemListedDate", currentTimeString);
        docContent14.put("latitude", "60.1445813");
        docContent14.put("longitude", "25.0508881");
        docContent14.put("contactInfo", "ken@gmail.com");
        docContent14.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/hyperg1ant-1a512.appspot.com/o/itemImages%2Ftvremote.JPG?alt=media&token=8679d51d-907c-4166-a9f3-413ea90f567f");

        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent14);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }

        // ITEM 15
        Map<String, Object> docContent15 = new HashMap<String, Object>();
        docContent15.put("title", "Scissors");
        docContent15.put("price", 2);
        docContent15.put("description", "Bla bla bla bla bla");
        docContent15.put("itemListedDate", currentTimeString);
        docContent15.put("latitude", "60.1445813");
        docContent15.put("longitude", "25.0508881");        docContent15.put("contactInfo", "505-555-555");
        docContent15.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/hyperg1ant-1a512.appspot.com/o/itemImages%2Fscissors.JPG?alt=media&token=090801c8-6448-4d2a-bdb5-c61a1279030d");
        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent15);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }

        // ITEM 16
        Map<String, Object> docContent16 = new HashMap<String, Object>();
        docContent16.put("title", "Wooden Box");
        docContent16.put("price", 1000000);
        docContent16.put("description", "Bla bla bla bla bla");
        docContent16.put("itemListedDate", currentTimeString);
        docContent16.put("latitude", "60.1445813");
        docContent16.put("longitude", "25.0508881");
        docContent16.put("contactInfo", "ken@gmail.com");
        docContent16.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/hyperg1ant-1a512.appspot.com/o/itemImages%2Fwoodenbox.JPG?alt=media&token=32752600-eaf3-45ec-ad68-97d1e4f1f44f");

        document = database.getDocument(docItemForSaleId + UUID.randomUUID().toString());
        try {
            document.putProperties(docContent16);
            Log.d(TAG, "Document written to database named " + dbname + " with ID = " + document.getId());
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Cannot write document to database", e);
        }
    }

    private void getLocation(){
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        fusedLocationProviderApi = LocationServices.FusedLocationApi;
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
        while(googleApiClient.isConnected()){


        }
    }
    @Override
    public void onLocationChanged(Location location) {
    }

    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    public void onProviderEnabled(String s) {
    }

    public void onProviderDisabled(String s) {
    }

    @Override
    public void onConnected(Bundle bundle) {
        currentLocation = fusedLocationProviderApi.getLastLocation(googleApiClient);
        if (currentLocation != null){
            setItemListDisplay();
        }
    }
    public void setItemListDisplay(){
        if(getIntent().getExtras() != null){
            Toast.makeText(MainActivity.this, this.getIntent().getExtras().getString("message"),Toast.LENGTH_LONG).show();
        }
        mGridView = (GridView) findViewById(R.id.gridView);
        //mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mGridData = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);

        parseResult();
        //mProgressBar.setVisibility(View.VISIBLE);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Item item = (Item) parent.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, ItemViewActivity.class);
                ImageView imageView = (ImageView) v.findViewById(R.id.grid_item_image);
                int[] screenLocation = new int[2];
                imageView.getLocationOnScreen(screenLocation);
                intent.putExtra("itemId", item.itemId);
                Log.d(TAG, intent.getStringExtra("itemId"));
                startActivity(intent);
            }


        });


        addItem = (Button) findViewById(R.id.btAddItem);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, postItemActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("LocationFragment", "Connection failed: ConnectionResult.getErrorCode() " + result.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();

    }


}