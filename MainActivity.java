package edu.pda.lba;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{

    private CountDownTimer timer;
    private int mAdCounter;
    private int mAdSkipper;

    private String mInput;
    private TextView tvStatus;
    private TextView tvDescription;

    private String mCurrentAdWebsite;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private boolean mSkipAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAds();
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        tvStatus = (TextView)findViewById(R.id.textViewStatus);
        tvDescription = (TextView)findViewById(R.id.textViewDescription);
        startAds();
    }

    private void startAds(){

        mAdCounter=0;
        mAdSkipper = 0;

        timer = new CountDownTimer(15000, 1000) {

            public void onTick(long millisUntilFinished) {

                if (mSkipAd)
                {

                    cancel();
                    onFinish();
                }
                else {
                    tvStatus.setText("seconds remaining: " + millisUntilFinished / 1000);
                }
            }
            public void onFinish() {
                tvStatus.setText("Loading Next Ad!");
                loadNextAd();
            }
        };
        new Connection().execute();

    }

    private void loadNextAd()
    {
        try {


            JSONArray jsonRoot = new JSONArray(mInput);
            Log.i("passed",mInput);
           // JSONArray jsonArray = jsonRoot.optJSONArray("Ads");
            //int num_ads = jsonArray.length();
            int num_ads = jsonRoot.length();


            JSONObject jsonAd = jsonRoot.getJSONObject(mAdCounter);
            String ad_id = jsonAd.optString("ad_id").toString();
            String ad_name = jsonAd.optString("ad_name").toString();
            String ad_category = jsonAd.optString("category").toString();
            String ad_image = jsonAd.optString("ad_image").toString();
            String ad_locality = jsonAd.optString("ad_locality").toString();
            String ad_description = jsonAd.optString("ad_description").toString();
           mCurrentAdWebsite = jsonAd.optString("website").toString();


            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String pref_adCategory = prefs.getString("pref_adCategory", "Auto");
            String pref_adDistance = prefs.getString("pref_adDistance", "1000");

           // double ad_distance = Double.parseDouble(null);
           double ad_distance = getDistance(ad_locality);
           if ((pref_adCategory.equals("Auto") || ad_category.equals(pref_adCategory))  &&
                    (ad_distance <= Double.parseDouble(pref_adDistance)))
         if(true)
                    {
                        float km = (float) (ad_distance/1000);
                    tvDescription.setText(new DecimalFormat("##.##").format(km)+ " Km away from " + ad_name+" ("+ ad_description+")");
                    new DownloadImageTask((ImageView) findViewById(R.id.imageView))
                            .execute(ad_image);
                        mSkipAd = false;
            }
            else{
                mAdSkipper++;
                if (mAdSkipper >= num_ads)
                {
                    tvStatus.setText("No ads match your preferences. Try other settings.");
                    mAdSkipper = 0;
                    return;
                }
                mSkipAd = true;
            }
            //mAdCounter = (mAdCounter+1)%num_ads;
            mAdCounter++;
            if (mAdCounter >= num_ads)
            {
                mAdSkipper = 0;
            }
            mAdCounter = (mAdCounter)%num_ads;
            timer.start();

        }
        catch (Exception e) {
            tvStatus.setText(e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
                Intent intent = new Intent(this, AdPreferenceActivity.class);
                startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        super.onResume();
    }

    private void showStatus(String status) {
        tvStatus.setText(status);
    }

    private class Connection extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... params) {
            try {
                mInput = null;
                StringBuffer sb = new StringBuffer();


             String link = ""; // put your link here
                URL url = new URL(link);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection .setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(60000 /* milliseconds */);

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line = "";
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                in.close();
                urlConnection.disconnect();

                mInput = sb.toString();
                Log.i("passed",mInput);
            }
            catch (Exception e) {
                e.printStackTrace();
                mInput = null;
            }

         //   String s = "{ \"Ads\": "+mInput+"}";
          //      Log.i("passed",s);
            return  mInput;
        }

        protected void onPostExecute(String result) {

            if (mInput == null){
                showStatus("Error in downloading ads");
            }
            else{
                showStatus("Ads loaded");

                Toast.makeText(getApplicationContext(), "Ads loaded", Toast.LENGTH_SHORT ).show();

                SharedPreferences pref = getSharedPreferences("LBAPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = pref.edit();
                edit.putString("AdsInput", mInput.toString());
                edit.commit();

                loadNextAd();
            }
        }
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            Log.i("passed",urls[0]);
            String urldisplay = ""+urls[0]; // put your link here
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return mIcon11;
        }
        protected void onPostExecute(Bitmap result) {
            if (result == null){
                tvStatus.setText("Cannot show ad image");
            }
            else{
                bmImage.setImageBitmap(result);
            }
        }
    }

    public void showNext(View v)
    {
        mSkipAd = true;
        //loadNextAd();
    }

    public void showMore(View v)
    {
        Intent intent = new Intent(this, MoreActivity.class);
        startActivity(intent);
    }

    public void showWebsite(View v)
    { String s = null;
        String url = mCurrentAdWebsite;

        if(url.equals("NF"))
        {
            Toast.makeText(getApplicationContext(), "No Website found", Toast.LENGTH_SHORT ).show();
        }
        else {
            s = "https://" + url;
            Log.i("passed", s);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
            startActivity(browserIntent);
        }

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently
        showStatus("Error in getting current location");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        showStatus("Connection to location services suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);


    }

    private double getDistance(String locality){
        double distance = 0;

        if (mLastLocation != null) {
            double latitude=0, longitude=0;
            try {
                Geocoder coder = new Geocoder(this);
                String s = "";
                if(locality.equals(s))
                    locality = "";
                ArrayList<Address> addresses = (ArrayList<Address>) coder.getFromLocationName( locality + "city name", 50);
                for (Address add : addresses) {
                    longitude = add.getLongitude();
                    latitude = add.getLatitude();
                    break;
                }
            }
            catch(Exception e) {
                tvDescription.setText("Error in geocoding");
            }

            Location adLocation = new Location("Ad Location");
            adLocation.setLatitude(latitude);
            adLocation.setLongitude(longitude);
            distance = mLastLocation.distanceTo(adLocation);
            Log.i("hi",String.valueOf(distance));
            tvDescription.setText(String.valueOf(longitude) + "," + String.valueOf(latitude));
        }
        else
        {
            tvDescription.setText("Location is null");
        }
        return distance;
    }
}

