package com.example.syed.squarerotate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.nfc.cardemulation.CardEmulation;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.crypto.Mac;

public class MainActivity extends AppCompatActivity {

    private TextView mTimeText;
    private ImageView mSquare;

    private boolean GET_TIME = false;
    private boolean QUIT_TASK = false;



    private final String KEY_DATE_TIME= "datetime";

    protected JSONObject mTimeData;

    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTimeText =  findViewById(R.id.timeText);
        mSquare = findViewById(R.id.squareView);

        Animation rotation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate);
        rotation.setFillAfter(true);
        mSquare.startAnimation(rotation);

        if (isNetworkAvailable()) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    final GetBlogPostTasks getBlogPostsTask = new GetBlogPostTasks();
                    getBlogPostsTask.execute();
                    handler.postDelayed(this, 1); //now is every second
                }
            }, 1); //Every second
        }
        else
        {
            Toast.makeText(this, "Network is unavailable!", Toast.LENGTH_LONG).show();
        }

    }

    private boolean isNetworkAvailable() {

        ConnectivityManager manager  = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo!= null && networkInfo.isConnected())
        {
            isAvailable = true;
        }

        return isAvailable;
    }

    public void handleBlogResponse() {

        if (mTimeData == null) {
            updateDisplayForError();
        }
        else {
            try {
                String datetime = mTimeData.get("datetime").toString();
                //DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSZ");
                Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(datetime);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int hours = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);
                int seconds = calendar.get(Calendar.SECOND);
                int milli = calendar.get(Calendar.MILLISECOND);

                String dateText = "" + hours + ":" + minutes + ":" + seconds;
                mTimeText.setText(dateText);

            }
            catch (JSONException e) {
                logException(e);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void logException(Exception e) {
        Log.e(TAG, "Exception caught!", e);
    }
    private void updateDisplayForError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));
        builder.setMessage(getString(R.string.error_message));
        builder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

        mTimeText.setText("Error");
    }

    private class GetBlogPostTasks extends AsyncTask<Object, Void, JSONObject>
    {

        void Sleep (int ms)
        {
            try
            {
                Thread.sleep(ms);
            }
            catch (Exception e )
            {

            }
        }

        @Override
        protected JSONObject doInBackground(Object... params) {
            int responseCode = -1;
            JSONObject jsonResponse = null;

            try
            {
                URL blogURL = new URL ("https://dateandtimeasjson.appspot.com");
                HttpURLConnection connection = (HttpURLConnection) blogURL.openConnection();
                connection.connect();

                responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    Reader reader = new InputStreamReader(inputStream);
                    int contentLength = connection.getContentLength();
                    char[] charArray = new char[contentLength];
                    reader.read(charArray);
                    String responseData = new String(charArray);

                    jsonResponse = new JSONObject(responseData);
                }
                else {
                    Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
                }
            }
            catch (MalformedURLException e)
            {
                Log.e(TAG, "Malformed Exception caught: ", e);
            }
            catch (IOException e)
            {
                Log.e(TAG, " IOException caught: ", e);
            }
            catch (Exception e)
            {
                Log.e (TAG, " Exception caught: ", e);
            }

            Log.i(TAG,"Code:"+responseCode);
            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            mTimeData = result;
            handleBlogResponse();
        }
    }

}
