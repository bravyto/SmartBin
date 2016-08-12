package com.mmcrajawali.smartbin;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bambang on 6/16/2016.
 */


public class LoginActivity extends AppCompatActivity {

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private UserLoginTask mAuthTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        SharedPreferences sharedPref= getSharedPreferences("app data", Context.MODE_PRIVATE);
        mEmailView = (EditText) findViewById(R.id.username);
        mEmailView.setText(sharedPref.getString("username", ""));
        mPasswordView = (EditText) findViewById(R.id.password);


        Button mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
//        mProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(LoginActivity.this.findViewById(R.id.login_page).getWindowToken(), 0);

        if (mAuthTask != null) {
            mAuthTask.cancel(true);
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
//            showProgress(true);
            mAuthTask = new UserLoginTask(email.replace(" ", "%20"), password.replace(" ", "%20"));
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {

                }
            });

//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mProgressView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//                }
//            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
//            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String mEmail;
        private final String mPassword;
        Exception mException = null;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        private Exception exception;
        private ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        InputStream inputStream = null;
        String result = "";

        protected void onPreExecute() {
            progressDialog.setMessage("Logging in...");
            progressDialog.show();
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    UserLoginTask.this.cancel(true);
                }
            });
        }

        @Override
        protected String doInBackground(Void... params) {
            String urlString = "http://mmcrajawali.com/login.php?username=" + mEmail + "&password=" + mPassword;
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
            mAuthTask = null;
//            showProgress(false);
            final JSONObject json;
            String status = null, role = null, id = null, name = null, truck_name = null, truck_number = null, truck_status = null;
            try {
                json = new JSONObject(stripHtml(success));
//                Log.e("statusnya", success);
                String encodedString = json.getString("status");
                status = encodedString;
//                Log.e("statusnya", status);
                encodedString = json.getString("role");
                role = encodedString;
//                Log.e("rolenya", role);
                if(role.equals("supir_truk")) {
                    encodedString = json.getString("id_user");
                    id = encodedString;
                    encodedString = json.getString("name");
                    name = encodedString;
                    encodedString = json.getString("nama_truk");
                    truck_name = encodedString;
                    encodedString = json.getString("nomor_truk");
                    truck_number = encodedString;
                    encodedString = json.getString("status_truk");
                    truck_status = encodedString;
                }



                if (role != null)
                    if (status.equals("sukses") && role.equals("supir_truk")) {
                        SharedPreferences sharedPref = getSharedPreferences("app data", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("id", id);
                        //                Log.e("idnya", id);
                        editor.putString("username", mEmail);
                        editor.putString("name", name);
                        editor.putString("truck_name", truck_name);
                        editor.putString("truck_number", truck_number);
                        editor.putString("truck_status", truck_status);
                        editor.commit();
                        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(myIntent);
                        finish();
                    } else {
                        mEmailView.setError("This username or password is incorrect");
                        mEmailView.requestFocus();
                    }
                else {
                    mEmailView.setError("This username or password is incorrect");
                    mEmailView.requestFocus();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Snackbar.make(findViewById(R.id.login_page), "No Connection",
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
            progressDialog.cancel();
        }

        @Override
        protected void onCancelled() {
            mAuthTask.cancel(true);
            mAuthTask = null;
//            showProgress(false);
        }
    }
}
