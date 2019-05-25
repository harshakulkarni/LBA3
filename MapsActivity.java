package edu.pda.lba;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String mInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        */

        new Connection().execute();
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
                urlConnection .setConnectTimeout(60000 /* milliseconds */);

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line = "";
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
                in.close();
                urlConnection.disconnect();
                mInput = sb.toString();
            }
            catch (Exception e) {
                e.printStackTrace();
                mInput = null;
            }
            return  null;
        }

        protected void onPostExecute(String result) {
            if (mInput != null){
                Toast.makeText(getApplicationContext(), "Advertiser info loaded", Toast.LENGTH_SHORT).show();
                loadAdvertisers();
            }
            else {
                Toast.makeText(getApplicationContext(), "Failed to load Advertiser info", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadAdvertisers()
    {
        try {
            Geocoder coder = new Geocoder(this);

            JSONArray jsonRoot = new JSONArray(mInput);
            //JSONArray jsonArray = jsonRoot.optJSONArray("Advertisers");
            final List<String> adList = new ArrayList<String>();
            final List<String> emailList = new ArrayList<String>();
            final List<String> mobileList = new ArrayList<String>();
            final List<String> localityList = new ArrayList<String>();
            final List<String> addressList = new ArrayList<String>();

            for(int i=0; i< jsonRoot.length(); i++){
                JSONObject jsonAd = jsonRoot.getJSONObject(i);
                //String firmId = jsonAd.optString("FirmId").toString();
                String name = jsonAd.optString("ad_name").toString();
               // String email = jsonAd.optString("Email").toString();
               // String mobile = jsonAd.optString("Mobile").toString();
                String locality = jsonAd.optString("ad_locality").toString();
                //String address = jsonAd.optString("Address").toString();




                try {

                    ArrayList<Address> addresses = (ArrayList<Address>) coder.getFromLocationName(locality+"city name", 50);
                    for(Address add : addresses){
                        double longitude = add.getLongitude();
                        double latitude = add.getLatitude();
                        LatLng place = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions().position(place).title(name + "(" + name + ")" + locality));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 15));
                        break;
                    }
                } catch (IOException e) {
                    //e.printStackTrace();
                }


            }






        }
        catch (Exception e) {
        }
    }



}
