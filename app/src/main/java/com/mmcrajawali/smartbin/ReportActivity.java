package com.mmcrajawali.smartbin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Bambang on 7/2/2016.
 */
public class ReportActivity extends AppCompatActivity {

    private Toolbar toolbar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        //Membuat Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Lapor");
        //getSupportActionBar().setSubtitle("");
        getSupportActionBar().setElevation(4);

        final EditText reportTitle = (EditText) findViewById(R.id.title_report);
        final EditText reportText = (EditText) findViewById(R.id.description_report);

        Button sendReport = (Button) findViewById(R.id.button_report);
        sendReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(ReportActivity.this.findViewById(R.id.report_layout).getWindowToken(), 0);
                boolean notFilled = true;
                reportTitle.setError(null);
                reportText.setError(null);
                if (reportTitle.getText().toString().equals("")) {
                    reportTitle.setError("Isi judul laporan anda!");
                } else {
                    if (reportText.getText().toString().equals("")) {
                        reportText.setError("Isi deskripsi laporan anda!");
                    } else {
                        notFilled = false;
                    }
                }

                if (!notFilled) {
                    SharedPreferences sharedPreferences = getSharedPreferences("app data", Context.MODE_PRIVATE);
                    AsyncTask blabla = new PostReport(sharedPreferences.getString("id", ""),reportTitle.getText().toString().replace(" ", "%20"),reportText.getText().toString().replace(" ", "%20"));
                    Void[] param = null;
                    blabla.execute(param);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_basic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class PostReport extends AsyncTask<Void, Void, String> {

        private final String mUserId, mTitle, mText;

        PostReport(String userId, String title, String text) {
            mUserId = userId;
            mTitle = title;
            mText = text;
        }

        private ProgressDialog progressDialog = new ProgressDialog(ReportActivity.this);
        protected void onPreExecute() {

            progressDialog.setMessage("Posting Report...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    PostReport.this.cancel(true);
                }
            });
        }

        @Override
        protected String doInBackground(Void... params) {
            String urlString = "http://mmcrajawali.com/report.php?userId=" + mUserId + "&title=" +mTitle+"&message=" +mText;
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
                if (json.getString("status").equals("gagal")) {
                    Snackbar.make(findViewById(R.id.report_layout), "Failed posting report",
                            Snackbar.LENGTH_SHORT)
                            .show();
                } else {
                    Snackbar.make(findViewById(R.id.report_layout), "Success posting report",
                            Snackbar.LENGTH_SHORT)
                            .show();
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
