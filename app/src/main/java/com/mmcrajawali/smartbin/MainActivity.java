package com.mmcrajawali.smartbin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bambang on 6/16/2016.
 */


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private List<Task> taskList = new ArrayList<>();
    private TaskAdapter mAdapter;
    private ArrayList name, latitude, longitude;
    private ArrayList<Marker> marker;
    private Switch fullSwitch;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<Polyline> polylinesnya = new ArrayList<Polyline>();

    private GoogleMap mMap;
    private int locator;
    private boolean autoRecenter;;
    private boolean mapTouched;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    public int routeCounter;

    private Polyline line = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inisialisasi variabel
        name = new ArrayList();
        latitude = new ArrayList();
        longitude = new ArrayList();
        marker = new ArrayList<Marker>();
        routeCounter = 0;
        mapTouched = false;
        mAdapter = new TaskAdapter(taskList);
        locator = 0;
        autoRecenter = true;

        //Set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
//        params.setScrollFlags(0);  // clear all scroll flags
        setSupportActionBar(toolbar);

        //Inisialisasi recyclerview
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);


        LocationManager_check locationManagerCheck = new LocationManager_check(this);

        if(locationManagerCheck.isLocationServiceAvailable()) {

        }else{
            locationManagerCheck .createLocationServiceError(MainActivity.this);
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
//        ViewGroup.LayoutParams params = mapFragment.getView().getLayoutParams();
//        params.height = 900;
//        mapFragment.getView().setLayoutParams(params);
        mapFragment.getMapAsync(this);

        View mapTouchLayer = findViewById(R.id.map_touch_layer);
        mapTouchLayer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mapTouched = true;
                autoRecenter = false;
                return false; // Pass on the touch to the map or shadow layer.
            }
        });

        fullSwitch = (Switch) findViewById(R.id.switch2);
        fullSwitch.setVisibility(View.GONE);

        fullSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    FullCapacityDialog();
                }
            }
        });

    }

    private void FullCapacityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kapasitas Penuh");
        builder.setMessage("Apakah Anda yakin truk sudah penuh?");
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                for (int i = 0; i < polylinesnya.size(); i++) {
                    polylinesnya.get(i).remove();
                }
                polylinesnya.clear();
                Location location = mMap.getMyLocation();
                String urlnya = makeURL(location.getLatitude(), location.getLongitude(), Double.parseDouble((String) latitude.get(latitude.size() - 1)), Double.parseDouble((String) longitude.get(longitude.size() - 1)));
                AsyncTask blabla = new connectAsyncTask(urlnya);
                Object[] arg = new String[]{null, null, null};
                blabla.execute(arg);
                fullSwitch.setClickable(false);
                mAdapter.setTaskClickable(false);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Keluar")
                .setMessage("Apakah kamu yakin ingin keluar?")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setNegativeButton("Tidak", null).show();
    }

    private boolean alreadyActivated;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.myswitch);
        item.setActionView(R.layout.switch_layout);
        alreadyActivated = false;
        final SwitchCompat actionView = (SwitchCompat) item.getActionView().findViewById(R.id.switchForActionBar);

        actionView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    if (activateTaskInFragment())
                        actionView.setClickable(false);
                    else {
                        if (alreadyActivated) {
                            actionView.setChecked(true);
                            actionView.setClickable(false);
                        } else
                            actionView.setChecked(false);
                    }
                }
            }
        });

        setUpMapIfNeeded();

        SharedPreferences sharedPreferences = getSharedPreferences("app data", Context.MODE_PRIVATE);
        if(!sharedPreferences.getString("truck_status", "").equals("idle") && mMap != null) {
            alreadyActivated = true;
            actionView.setChecked(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_report:
                Intent i = new Intent(getApplicationContext(), ReportActivity.class);
                startActivity(i);
                return true;
            case R.id.action_rules:
                Intent j = new Intent(getApplicationContext(), RulesActivity.class);
                startActivity(j);
                return true;
            case R.id.action_logout:
                Intent k = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(k);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean activateTaskInFragment() {

        Location location = mMap.getMyLocation();
        if (location != null) {
            name.add("My Position");
            latitude.add(location.getLatitude() + "");
            longitude.add(location.getLongitude() + "");
            SharedPreferences sharedPref = getSharedPreferences("app data", Context.MODE_PRIVATE);
            AsyncTask activateTask = new ActivateTask(sharedPref.getString("id", ""));
            Void[] param = null;
            activateTask.execute(param);
            return true;
        } else {
                Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Cannot find GPS signal",
                        Snackbar.LENGTH_SHORT)
                        .show();
            return false;
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.

            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setMyLocationEnabled(true);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        Location location = mMap.getMyLocation();
                        if (location != null) {
                            LatLng myPosition = new LatLng(location.getLatitude(), location.getLongitude());

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myPosition.latitude, myPosition.longitude), 17.0f));
                            autoRecenter = true;
                        } else {
                            Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Cannot find GPS signal",
                                    Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                        return true;
                    }
                });

                mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                    @Override
                    public void onMyLocationChange(Location arg0) {
                        // TODO Auto-generated method stub
                        //send position to website
                        LatLng myPosition = new LatLng(arg0.getLatitude(), arg0.getLongitude());
                        if (autoRecenter) {
                            //mMap.addMarker(new MarkerOptions().position(myPosition).title("It's Me!"));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myPosition.latitude, myPosition.longitude), 17.0f));
                        }
                        SharedPreferences sharedPref = getSharedPreferences("app data", Context.MODE_PRIVATE);
                        AsyncTask updatePos = new UpdateTask(sharedPref.getString("id", ""), arg0.getLatitude() + "", arg0.getLongitude() + "");
                        Void[] param = null;
                        updatePos.execute(param);
                        if (alreadyActivated)
                            if (activateTaskInFragment())
                                alreadyActivated = false;
                    }
                });

            }

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
        // Add a marker in Sydney and move the camera
        //dummy local
    }

    public String makeURL (double sourcelat, double sourcelog, double destlat, double destlog ){
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        //https://maps.googleapis.com/maps/api/directions/json?origin=Adelaide,SA&destination=Adelaide,SA&waypoints=optimize:true|Barossa+Valley,SA|Clare,SA|Connawarra,SA&sensor=false&mode=driving&alternatives=true&key=AIzaSyDjkNXLI4j-k4ZhdSA3WkHxLUyXagm5aH8
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString
                .append(Double.toString(sourcelog));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true&avoidHighways=true");
        urlString.append("&key=AIzaSyDjkNXLI4j-k4ZhdSA3WkHxLUyXagm5aH8");
        return urlString.toString();
    }

    public void drawPath(String  result) {

        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            line = mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(12)
                    .color(Color.parseColor("#05b1fb"))//Google maps blue color
                    .geodesic(true)
            );

            polylinesnya.add(line);
           /*
           for(int z = 0; z<list.size()-1;z++){
                LatLng src= list.get(z);
                LatLng dest= list.get(z+1);
                Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
                .width(2)
                .color(Color.BLUE).geodesic(true));
            }
           */
        }
        catch (JSONException e) {

        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

    private class connectAsyncTask extends AsyncTask<String, String, String> {
        private ProgressDialog progressDialog;
        String url;
        connectAsyncTask(String urlPass){
            url = urlPass;
        }
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
//            Log.e("url di async", url);
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }
        @Override
        protected String doInBackground(String... params) {
            JSONParser jParser = new JSONParser();
//            Log.e("url di doinbackground", url);
            String json = jParser.getJSONFromUrl(url);
            return json;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.hide();
            if(result!=null){
                drawPath(result);
            }
        }
    }

    public class ActivateTask extends AsyncTask<Void, Void, String> {

        private final String mUserId;
        Exception mException = null;

        ActivateTask(String userId) {
            mUserId = userId;
        }

        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        protected void onPreExecute() {
            progressDialog.setMessage("Activating...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    ActivateTask.this.cancel(true);
                }
            });
        }

        @Override
        protected String doInBackground(Void... params) {
            String urlString = "http://mmcrajawali.com/tasks.php?userId=" + mUserId;
            JSONParser jParser = new JSONParser();
            String json = jParser.getJSONFromUrl(urlString);
            return json;
        }

        public String stripHtml(String html) {
            return Html.fromHtml(html).toString();
        }

        @Override
        protected void onPostExecute(final String success) {
            try {
                JSONObject status = new JSONObject(stripHtml(success));
                Log.e("status", status.getString("status"));
                if (status.getString("status").equals("gagal")) {
                    Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Failed activating",
                            Snackbar.LENGTH_SHORT)
                            .show();
                } else {
                    JSONArray point = status.getJSONArray("tp");
                    String idTPA = null;
                    String namaTPA = null;
                    String latTPA = null;
                    String logTPA = null;
                    SharedPreferences sharedPref = getSharedPreferences("app data", Context.MODE_PRIVATE);
                    for (int i = 0; i < point.length(); i++) {
                        JSONObject e = point.getJSONObject(i);
                        String role = e.getString("role");
                        if (role.equals("tpa")) {
                            idTPA = e.getString("id_tp");
//                            Log.e("budi", idTPA);
                            namaTPA = e.getString("name");
                            latTPA = e.getString("latitude");
                            logTPA = e.getString("longitude");
                        } else {
                            taskList.add(new Task(sharedPref.getString("id", ""), e.getString("id_tp"), e.getString("name"), "tps", Double.parseDouble(e.getString("latitude")), Double.parseDouble(e.getString("longitude"))));
                            name.add(e.getString("name"));
                            latitude.add(e.getString("latitude"));
                            longitude.add(e.getString("longitude"));
                            LatLng TPS = new LatLng(Double.parseDouble(e.getString("latitude")), Double.parseDouble(e.getString("longitude")));
                            mMap.addMarker(new MarkerOptions().position(TPS).title(e.getString("name")));
                        }
                    }
                    taskList.add(new Task(sharedPref.getString("id", ""), idTPA, namaTPA, "tpa", Double.parseDouble(latTPA), Double.parseDouble(logTPA)));
                    name.add(namaTPA);
                    latitude.add(latTPA);
                    longitude.add(logTPA);
                    mAdapter.notifyDataSetChanged();

                    fullSwitch.setVisibility(View.VISIBLE);
                    fullSwitch.setClickable(true);
                    fullSwitch.setChecked(false);

                    LatLng TPA = new LatLng(Double.parseDouble(latTPA), Double.parseDouble(logTPA));

                    marker.add(mMap.addMarker(new MarkerOptions().position(TPA).title(namaTPA)));

                    for (int j = 0; j < name.size() - 1; j++) {
                        String urlnya = makeURL(Double.parseDouble((String) latitude.get(j)), Double.parseDouble((String) longitude.get(j)), Double.parseDouble((String) latitude.get(j + 1)), Double.parseDouble((String) longitude.get(j + 1)));
                        AsyncTask blabla = new connectAsyncTask(urlnya);
                        Object[] arg = new String[]{null, null, null};
                        blabla.execute(arg);
                    }
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            progressDialog.cancel();
        }

        @Override
        protected void onCancelled() {
        }
    }
    public class UpdateTask extends AsyncTask<Void, Void, String> {

        private final String mUserId, mLatitude, mLongitude;

        UpdateTask(String userId, String latitude, String longitude) {
            mUserId = userId;
            mLatitude = latitude;
            mLongitude = longitude;
        }

        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... params) {
            String urlString = "http://mmcrajawali.com/updatePosisi.php?userId=" + mUserId + "&latitude=" +mLatitude+"&longitude=" +mLongitude;
            JSONParser jParser = new JSONParser();
            String json = jParser.getJSONFromUrl(urlString);
            return json;
        }

        @Override
        protected void onPostExecute(final String success) {

        }

        @Override
        protected void onCancelled() {

        }
    }


    public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.MyViewHolder> {

        private List<Task> taskListAdapter;
        private ArrayList<View> row = new ArrayList<View>();

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView location_name;
            public CheckBox checkbox;

            public MyViewHolder(View view) {
                super(view);
                location_name = (TextView) view.findViewById(R.id.location_name);
                checkbox = (CheckBox) view.findViewById(R.id.checkBox);
            }
        }

        public TaskAdapter(List<Task> taskList) {
            this.taskListAdapter = taskList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.task_list_row, parent, false);
            row.add(itemView);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final Task task = taskListAdapter.get(position);
            holder.location_name.setText(task.getLocationName());
            holder.checkbox.setChecked(false);
            holder.checkbox.setClickable(true);
            holder.checkbox.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Location recent = mMap.getMyLocation();
                    Location tp_loc = new Location("");
                    tp_loc.setLatitude(task.getLocation_latitude());
                    tp_loc.setLongitude(task.getLocation_longitude());
                    Log.e("jaraknya", recent.distanceTo(tp_loc) + "");
                    if (recent.distanceTo(tp_loc) < 20) {
                        if (((CheckBox) v).isChecked()) {
                            AsyncTask blabla = new TaskDone(task.getDriver_id(), task.getLocationId());
                            Void[] param = null;
                            blabla.execute(param);
                            ((CheckBox) v).setClickable(false);
                            if (task.getLocation_type().equals("tpa")) {
                                AsyncTask blibli = new GoHome(task.getDriver_id());
                                blibli.execute(param);
                                taskList.clear();
                                taskListAdapter.clear();
                                row.clear();
                                mAdapter.notifyDataSetChanged();
                                fullSwitch.setVisibility(View.GONE);
                                for (int i = 0; i < polylinesnya.size(); i++) {
                                    polylinesnya.get(i).remove();
                                }
                                polylinesnya.clear();
                                name.clear();
                                latitude.clear();
                                longitude.clear();
                                for (int i = 0; i < marker.size(); i++
                                        ) {
                                    marker.get(i).remove();
                                }
                                marker.clear();
                            }
                        }
                    } else {
                        Snackbar.make(findViewById(R.id.myCoordinatorLayout), "You are too far from that location",
                                Snackbar.LENGTH_SHORT)
                                .show();
                        ((CheckBox) v).setChecked(false);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return taskListAdapter.size();
        }

        public ArrayList<View> getRow() {
            return row;
        }

        public void setTaskClickable(boolean order) {
            for (int i = 0; i < row.size() - 1; i++) {
                row.get(i).findViewById(R.id.checkBox).setClickable(order);
            }
        }


        public class TaskDone extends AsyncTask<Void, Void, String> {

            private final String mUserId;
            private final String mTPId;

            TaskDone(String userId, String TPId) {
                mUserId = userId;
                mTPId = TPId;
            }

            protected void onPreExecute() {

            }

            @Override
            protected String doInBackground(Void... params) {
                String urlString = "http://mmcrajawali.com/visited.php?userId=" + mUserId + "&tpId=" + mTPId;
//            String urlString = "http://mmcrajawali.com/smartbin/public/login?username=" + mEmail + "&password=" + mPassword;
//            String urlString = "http://mmcrajawali.com/smartbin/public/loginapi?username=" + mEmail + "&password=" + mPassword;
//            String urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=-6.366026,106.8279491&destination=-6.402457,106.8300367&sensor=false&mode=driving&alternatives=true&key=AIzaSyDjkNXLI4j-k4ZhdSA3WkHxLUyXagm5aH8";
                JSONParser jParser = new JSONParser();
                String json = jParser.getJSONFromUrl(urlString);
                return json;
            }

            @Override
            protected void onPostExecute(final String success) {

            }

            @Override
            protected void onCancelled() {
            }
        }
    }

    public class GoHome extends AsyncTask<Void, Void, String> {

        private final String mId;

        GoHome(String id) {
            mId = id;
        }

        private Exception exception;
        private ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        InputStream inputStream = null;
        String result = "";

        protected void onPreExecute() {
            progressDialog.setMessage("Fetching Base...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    GoHome.this.cancel(true);
                }
            });
        }

        @Override
        protected String doInBackground(Void... params) {
            String urlString = "http://mmcrajawali.com/taskDone.php?userId=" + mId;
//            String urlString = "http://mmcrajawali.com/smartbin/public/login?username=" + mEmail + "&password=" + mPassword;
//            String urlString = "http://mmcrajawali.com/smartbin/public/loginapi?username=" + mEmail + "&password=" + mPassword;
//            String urlString = "https://maps.googleapis.com/maps/api/directions/json?origin=-6.366026,106.8279491&destination=-6.402457,106.8300367&sensor=false&mode=driving&alternatives=true&key=AIzaSyDjkNXLI4j-k4ZhdSA3WkHxLUyXagm5aH8";
            JSONParser jParser = new JSONParser();
            String json = jParser.getJSONFromUrl(urlString);
            return json;
        }

        public String stripHtml(String html) {
            return Html.fromHtml(html).toString();
        }

        @Override
        protected void onPostExecute(final String success) {
            final JSONObject json;
            try {
                json = new JSONObject(stripHtml(success));
                if (json.getString("id_tp").equals("gagal")) {
                    Snackbar.make(findViewById(R.id.myCoordinatorLayout), "Failed fetching base",
                            Snackbar.LENGTH_SHORT)
                            .show();
                } else {
                    Location location = mMap.getMyLocation();
                    LatLng base = new LatLng(Double.parseDouble(json.getString("latitude")), Double.parseDouble(json.getString("longitude")));
                    mMap.addMarker(new MarkerOptions().position(base).title("Base"));
                    String urlnya = makeURL(location.getLatitude(), location.getLongitude(), Double.parseDouble(json.getString("latitude")), Double.parseDouble(json.getString("longitude")));
                    AsyncTask blabla = new connectAsyncTask(urlnya);
                    Object[] arg = new String[]{null, null, null};
                    blabla.execute(arg);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            progressDialog.cancel();
        }

        @Override
        protected void onCancelled() {

        }
    }
}
