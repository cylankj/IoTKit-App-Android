///*
// * Copyright (c) 2014 Google Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
// * in compliance with the License. You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software distributed under the License
// * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// * or implied. See the License for the specific language governing permissions and limitations under
// * the License.
// */
//
//package com.cylan.jiafeigou.rtmp.youtube;
//
//import android.Manifest;
//import android.accounts.AccountManager;
//import android.app.Activity;
//import android.app.Dialog;
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.preference.PreferenceManager;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.Window;
//import android.widget.Button;
//
//import com.android.volley.toolbox.ImageLoader;
//import com.cylan.jiafeigou.R;
//import com.cylan.jiafeigou.rtmp.youtube.util.EventData;
//import com.cylan.jiafeigou.rtmp.youtube.util.NetworkSingleton;
//import com.cylan.jiafeigou.rtmp.youtube.util.Utils;
//import com.cylan.jiafeigou.rtmp.youtube.util.YouTubeApi;
//import com.google.android.gms.common.GooglePlayServicesUtil;
//import com.google.api.client.extensions.android.http.AndroidHttp;
//import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
//import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
//import com.google.api.client.http.HttpTransport;
//import com.google.api.client.json.JsonFactory;
//import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.client.util.ExponentialBackOff;
//import com.google.api.services.youtube.YouTube;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//
///**
// * @author Ibrahim Ulukaya <ulukaya@google.com>
// *         <p/>
// *         Main activity class which handles authorization and intents.
// */
//public class MainActivity extends Activity implements
//        EventsListFragment.Callbacks {
//    public static final String ACCOUNT_KEY = "accountName";
//    public static final String APP_NAME = "WatchMe";
//    private static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
//    private static final int REQUEST_GMS_ERROR_DIALOG = 1;
//    private static final int REQUEST_ACCOUNT_PICKER = 2;
//    private static final int REQUEST_AUTHORIZATION = 3;
//    private static final int REQUEST_STREAMER = 4;
//    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
//    final JsonFactory jsonFactory = new GsonFactory();
//    GoogleAccountCredential credential;
//    private String mChosenAccountName;
//    private ImageLoader mImageLoader;
//    private EventsListFragment mEventsListFragment;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        ensureLoader();
//
//        credential = GoogleAccountCredential.usingOAuth2(
//                getApplicationContext(), Arrays.asList(Utils.SCOPES));
//        // set exponential backoff policy
//        credential.setBackOff(new ExponentialBackOff());
//
//        if (savedInstanceState != null) {
//            mChosenAccountName = savedInstanceState.getString(ACCOUNT_KEY);
//        } else {
//            loadAccount();
//        }
//
//        credential.setSelectedAccountName(mChosenAccountName);
//
//        mEventsListFragment = (EventsListFragment) getFragmentManager()
//                .findFragmentById(R.id.list_fragment);
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        // Here, thisActivity is the current activity
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.READ_CONTACTS)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.READ_CONTACTS)) {
//
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//
//            } else {
//
//                // No explanation needed, we can request the permission.
//
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.GET_ACCOUNTS}, 20);
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case 20: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted, yay! Do the
//                    // contacts-related task you need to do.
//                    Log.d("...", "granted");
//                } else {
//                    Log.d("...", "denied");
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//            }
//
//            // other 'case' lines to check for other
//            // permissions this app might request
//        }
//    }
//
//    public void startStreaming(EventData event) {
//
//
//        String broadcastId = event.getId();
//
//        new StartEventTask().execute(broadcastId);
//
//        Intent intent = new Intent(getApplicationContext(),
//                StreamerActivity.class);
//        intent.putExtra(YouTubeApi.RTMP_URL_KEY, event.getIngestionAddress());
//        intent.putExtra(YouTubeApi.BROADCAST_ID_KEY, broadcastId);
//
//        startActivityForResult(intent, REQUEST_STREAMER);
//
//    }
//
//    private void getLiveEvents() {
//        if (mChosenAccountName == null) {
//            return;
//        }
//        credential.setSelectedAccountName(mChosenAccountName);
//        new GetLiveEventsTask().execute();
//    }
//
//    public void createEvent(View view) {
//        new CreateLiveEventTask().execute();
//    }
//
//    private void ensureLoader() {
//        if (mImageLoader == null) {
//            // Get the ImageLoader through your singleton class.
//            mImageLoader = NetworkSingleton.getInstance(this).getImageLoader();
//        }
//    }
//
//    private void loadAccount() {
//        SharedPreferences sp = PreferenceManager
//                .getDefaultSharedPreferences(this);
//        mChosenAccountName = sp.getString(ACCOUNT_KEY, null);
//        invalidateOptionsMenu();
//    }
//
//    private void saveAccount() {
//        SharedPreferences sp = PreferenceManager
//                .getDefaultSharedPreferences(this);
//        sp.edit().putString(ACCOUNT_KEY, mChosenAccountName).apply();
//    }
//
//    private void loadData() {
//        if (mChosenAccountName == null) {
//            return;
//        }
//        getLiveEvents();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        getMenuInflater().inflate(R.menu.activity_main, menu);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_refresh:
//                loadData();
//                break;
//            case R.id.menu_accounts:
//                chooseAccount();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case REQUEST_GMS_ERROR_DIALOG:
//                break;
//            case REQUEST_GOOGLE_PLAY_SERVICES:
//                if (resultCode == Activity.RESULT_OK) {
//                    haveGooglePlayServices();
//                } else {
//                    checkGooglePlayServicesAvailable();
//                }
//                break;
//            case REQUEST_AUTHORIZATION:
//                if (resultCode != Activity.RESULT_OK) {
//                    chooseAccount();
//                }
//                break;
//            case REQUEST_ACCOUNT_PICKER:
//                if (resultCode == Activity.RESULT_OK && data != null
//                        && data.getExtras() != null) {
//                    String accountName = data.getExtras().getString(
//                            AccountManager.KEY_ACCOUNT_NAME);
//                    if (accountName != null) {
//                        mChosenAccountName = accountName;
//                        credential.setSelectedAccountName(accountName);
//                        saveAccount();
//                    }
//                }
//                break;
//            case REQUEST_STREAMER:
//                if (resultCode == Activity.RESULT_OK && data != null
//                        && data.getExtras() != null) {
//                    String broadcastId = data.getStringExtra(YouTubeApi.BROADCAST_ID_KEY);
//                    if (broadcastId != null) {
//                        new EndEventTask().execute(broadcastId);
//                    }
//                }
//                break;
//
//        }
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putString(ACCOUNT_KEY, mChosenAccountName);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//    }
//
//    @Override
//    public void onConnected(String connectedAccountName) {
//        // Make API requests only when the user has successfully signed in.
//        loadData();
//    }
//
//    public void showGooglePlayServicesAvailabilityErrorDialog(
//            final int connectionStatusCode) {
//        runOnUiThread(new Runnable() {
//            public void run() {
//                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
//                        connectionStatusCode, MainActivity.this,
//                        REQUEST_GOOGLE_PLAY_SERVICES);
//                dialog.show();
//            }
//        });
//    }
//
//    /**
//     * Check that Google Play services APK is installed and up to date.
//     */
//    private boolean checkGooglePlayServicesAvailable() {
//        final int connectionStatusCode = GooglePlayServicesUtil
//                .isGooglePlayServicesAvailable(this);
//        if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
//            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
//            return false;
//        }
//        return true;
//    }
//
//    private void haveGooglePlayServices() {
//        // check if there is already an account selected
//        if (credential.getSelectedAccountName() == null) {
//            // ask user to choose account
//            chooseAccount();
//        }
//    }
//
//    private void chooseAccount() {
//        startActivityForResult(credential.newChooseAccountIntent(),
//                REQUEST_ACCOUNT_PICKER);
//    }
//
//    @Override
//    public void onBackPressed() {
//    }
//
//    @Override
//    public ImageLoader onGetImageLoader() {
//        ensureLoader();
//        return mImageLoader;
//    }
//
//    @Override
//    public void onEventSelected(EventData liveBroadcast) {
//        startStreaming(liveBroadcast);
//    }
//
//    private class GetLiveEventsTask extends
//            AsyncTask<Void, Void, List<EventData>> {
//        private ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            progressDialog = ProgressDialog.show(MainActivity.this, null,
//                    getResources().getText(R.string.loadingEvents), true);
//        }
//
//        @Override
//        protected List<EventData> doInBackground(
//                Void... params) {
//            YouTube youtube = new YouTube.Builder(transport, jsonFactory,
//                    credential).setApplicationName(APP_NAME)
//                    .build();
//            try {
//                return YouTubeApi.getLiveEvents(youtube);
//            } catch (UserRecoverableAuthIOException e) {
//                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
//            } catch (IOException e) {
//                Log.e(MainActivity.APP_NAME, "", e);
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(
//                List<EventData> fetchedEvents) {
//            if (fetchedEvents == null) {
//                progressDialog.dismiss();
//                return;
//            }
//
//            mEventsListFragment.setEvents(fetchedEvents);
//            progressDialog.dismiss();
//        }
//    }
//
//    private class CreateLiveEventTask extends
//            AsyncTask<Void, Void, List<EventData>> {
//        private ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            progressDialog = ProgressDialog.show(MainActivity.this, null,
//                    getResources().getText(R.string.creatingEvent), true);
//        }
//
//        @Override
//        protected List<EventData> doInBackground(
//                Void... params) {
//            YouTube youtube = new YouTube.Builder(transport, jsonFactory,
//                    credential).setApplicationName(APP_NAME)
//                    .build();
//            try {
//                String date = new Date().toString();
//                YouTubeApi.createLiveEvent(youtube, "Event - " + date,
//                        "A live streaming event - " + date);
//                return YouTubeApi.getLiveEvents(youtube);
//
//            } catch (UserRecoverableAuthIOException e) {
//                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
//            } catch (IOException e) {
//                Log.e(MainActivity.APP_NAME, "", e);
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(
//                List<EventData> fetchedEvents) {
//
//            Button buttonCreateEvent = (Button) findViewById(R.id.create_button);
//            buttonCreateEvent.setEnabled(true);
//
//            progressDialog.dismiss();
//        }
//    }
//
//    private class StartEventTask extends AsyncTask<String, Void, Void> {
//        private ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            progressDialog = ProgressDialog.show(MainActivity.this, null,
//                    getResources().getText(R.string.startingEvent), true);
//        }
//
//        @Override
//        protected Void doInBackground(String... params) {
//            YouTube youtube = new YouTube.Builder(transport, jsonFactory,
//                    credential).setApplicationName(APP_NAME)
//                    .build();
//            try {
//                YouTubeApi.startEvent(youtube, params[0]);
//            } catch (UserRecoverableAuthIOException e) {
//                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
//            } catch (IOException e) {
//                Log.e(MainActivity.APP_NAME, "", e);
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void param) {
//            progressDialog.dismiss();
//        }
//
//    }
//
//    private class EndEventTask extends AsyncTask<String, Void, Void> {
//        private ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            progressDialog = ProgressDialog.show(MainActivity.this, null,
//                    getResources().getText(R.string.endingEvent), true);
//        }
//
//        @Override
//        protected Void doInBackground(String... params) {
//            YouTube youtube = new YouTube.Builder(transport, jsonFactory,
//                    credential).setApplicationName(APP_NAME)
//                    .build();
//            try {
//                if (params.length >= 1) {
//                    YouTubeApi.endEvent(youtube, params[0]);
//                }
//            } catch (UserRecoverableAuthIOException e) {
//                startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
//            } catch (IOException e) {
//                Log.e(MainActivity.APP_NAME, "", e);
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void param) {
//            progressDialog.dismiss();
//        }
//    }
//}
