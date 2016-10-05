package com.example.caleb.myjourney;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MyJourneyActivity extends AppCompatActivity {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader = new ArrayList<String>();
    HashMap<String, List<ListItem>> listDataChild = new HashMap<String, List<ListItem>>();
    int currentpos = -1, waittime = -1;
    private Flight flightInfo = null;
    Context c;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private String mActivityTitle;
    private ArrayAdapter<String> mAdapter;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_journey);

        mDrawerList = (ListView)findViewById(R.id.navList2);
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout2);
        mActivityTitle = getTitle().toString();

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Bundle bundle = getIntent().getExtras();
        waittime =bundle.getInt("waitTime");

        sendGetRequestFlightDetails();

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        // preparing list data
        //prepareListData();

        c = this;
        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);



        // Listview Group click listener
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // Toast.makeText(getApplicationContext(),
                // "Group Clicked " + listDataHeader.get(groupPosition),
                // Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
//                Toast.makeText(getApplicationContext(),
//                        listDataHeader.get(groupPosition) + " Expanded",
//                        Toast.LENGTH_SHORT).show();

                if (groupPosition != currentpos && currentpos != -1){
                    expListView.collapseGroup(currentpos);
                }
                currentpos = groupPosition;
            }
        });

        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
//                Toast.makeText(getApplicationContext(),
//                        listDataHeader.get(groupPosition) + " Collapsed",
//                        Toast.LENGTH_SHORT).show();

            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub
//                Toast.makeText(
//                        getApplicationContext(),
//                        listDataHeader.get(groupPosition)
//                                + " : "
//                                + listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition).getName(), Toast.LENGTH_SHORT)
//                        .show();
                switch(groupPosition){
                    case 0:
                        switch(childPosition){
                            case 0: //Manage Booking Details
                                Intent journey = new Intent(MyJourneyActivity.this, MainActivity.class);
                                startActivity(journey);
                                break;
                            case 1: //Apply Visa
                            case 2: //Explore
                        }
                        break;
                    case 1:
                        switch(childPosition){
                            case 0: //Check in online
                            default:
                                break;
                        }
                        break;
                    case 2: //Departure
                }


                return false;
            }
        });
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<ListItem>>();

        // Adding child data
        listDataHeader.add("Pre-Flight");
        listDataHeader.add("Check In");
        listDataHeader.add("Departure");
        listDataHeader.add("Arrival");

        // Adding child data
        List<ListItem> preFlight = new ArrayList<>();
        preFlight.add(new ListItem("Manage Booking Details", true));
        preFlight.add(new ListItem("Apply Visa", true));
        preFlight.add(new ListItem("Explore", true));

        List<ListItem> checkIn = new ArrayList<>();
        checkIn.add(new ListItem("Check In Online", true));
        checkIn.add(new ListItem("Estimated Waiting Time: " + waittime + " minutes"));
        checkIn.add(new ListItem("Flight Status: " + flightInfo.getStatusText()));

        List<ListItem> departure = new ArrayList<>();
        departure.add(new ListItem("Terminal"));
        departure.add(new ListItem("Gate: "));
        departure.add(new ListItem("Flight Details"));
        departure.add(new ListItem("Map", true));

        List<ListItem> arrival = new ArrayList<>();
        arrival.add(new ListItem("Flight Status"));
        arrival.add(new ListItem("Map", true));
        arrival.add(new ListItem("Explore", true));

        listDataChild.put(listDataHeader.get(0), preFlight); // Header, Child data
        listDataChild.put(listDataHeader.get(1), checkIn);
        listDataChild.put(listDataHeader.get(2), departure);
        listDataChild.put(listDataHeader.get(3), arrival);
    }

    public void sendGetRequestFlightDetails() {
        new MyJourneyActivity.GetFlightDetails(this).execute();
    }


    private class GetFlightDetails extends AsyncTask<String, Void, Integer> {

        private final Context context;

        public GetFlightDetails(Context c){
            this.context = c;
        }

        protected void onPreExecute(){
            /* progress= new ProgressDialog(this.context);
            progress.setMessage("Loading");
            progress.show(); */
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {

                // final TextView outputView = (TextView) findViewById(R.id.showOutput);
                final String baseURL = "https://flifo-qa.api.aero/flifo/v3/flight";
                Uri builtUri = Uri.parse(baseURL).buildUpon()
                        .appendPath("sin")
                        .appendPath("sq")
                        .appendPath("26")
                        .appendPath("d")
                        .build();
                Log.v("MainActivity", builtUri.toString());
                URL url = new URL(builtUri.toString());

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("X-apiKey", "2cfd0827f82ceaccae7882938b4b1627");

                BufferedReader streamReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                final StringBuilder responseStrBuilder = new StringBuilder();
                String inputStr = "";
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);
                Log.v("MainActivity", "String is : " + responseStrBuilder.toString());

                Flight flight_info = null;
                try {
                    JSONObject test = new JSONObject(responseStrBuilder.toString());
                    JSONArray flightRecord = test.getJSONArray("flightRecord");
                    JSONObject c = flightRecord.getJSONObject(0);
                    flight_info = new Flight(c);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                flightInfo = flight_info;
                Log.v("flightInfo", "TEST TEST TEST" +flightInfo.getStatusText());
                MyJourneyActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                       /* outputView.setText(responseStrBuilder);
                        progress.dismiss(); */
                    }
                });


            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return 1;
        }

        protected void onPostExecute(Integer result){
            Log.v("TEST", "Post executed");

            prepareListData();

            listAdapter = new ExpandableListAdapter(c, listDataHeader, listDataChild);

            // setting list adapter
            expListView.setAdapter(listAdapter);

            super.onPostExecute(null);
        }

    }

    // NAVIGATION DRAWER DETAILS

    private void addDrawerItems() {
        String[] osArray = { "Search Flights", "My Journey", "Check In", "Krisflyer", "Login", "Settings" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MyJourneyActivity.this, "Time for an upgrade!", Toast.LENGTH_SHORT).show();

                switch(position){
                    case 1:
                        Intent journey = new Intent(MyJourneyActivity.this, MyJourneyActivity.class);

                        // passing on the variables  needed in My Journey
                        journey.putExtra("waitTime", -1);
                        journey.putExtra("statusText", flightInfo.getStatusText());
                        journey.putExtra("scheduled", flightInfo.getScheduled());
                        journey.putExtra("terminal", flightInfo.getTerminal());
                        journey.putExtra("city", flightInfo.getCity());
                        //journey.putExtra("gate", flightInfo.getGate());

                        /* journey.putExtra("statusText", flightInfo.getStatusText());
                        journey.putExtra("statusText", flightInfo.getStatusText());
                        journey.putExtra("statusText", flightInfo.getStatusText()); */
                        startActivity(journey);
                    default:
                        break;
                }
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Menu");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_journey, menu);
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
            return true;
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
