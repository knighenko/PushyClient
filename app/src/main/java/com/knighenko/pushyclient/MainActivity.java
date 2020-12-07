package com.knighenko.pushyclient;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.net.URL;

import me.pushy.sdk.Pushy;
import me.pushy.sdk.util.exceptions.PushyException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pushy.listen(this);
        setContentView(R.layout.activity_main);

        if (!Pushy.isRegistered(this)) {
            new RegisterForPushNotificationsAsync(this).execute();
        }


        Intent myIntent = new Intent(MainActivity.this, Play.class);
        myIntent.putExtra("inputExtra", "Мониторинг в рубрике Дом и Сад");
        // Call startService with Intent parameter.
        this.startService(myIntent);
    }

    private class RegisterForPushNotificationsAsync extends AsyncTask<Void, Void, Object> {
        Activity mActivity;

        public RegisterForPushNotificationsAsync(Activity activity) {
            this.mActivity = activity;
        }

        protected Object doInBackground(Void... params) {
            try {
                // Register the device for notifications
                String deviceToken = Pushy.register(getApplicationContext());

                // Registration succeeded, log token to logcat
                Log.d("Pushy", "Pushy device token: " + deviceToken);

                // Send the token to your backend server via an HTTP GET request
                new URL("https://{YOUR_API_HOSTNAME}/register/device?token=" + deviceToken).openConnection();

                // Provide token to onPostExecute()
                return deviceToken;
            } catch (Exception exc) {
                // Registration failed, provide exception to onPostExecute()
                return exc;
            }
        }

        @Override
        protected void onPostExecute(Object result) {
            String message;

            // Registration failed?
            if (result instanceof Exception) {
                // Log to console
                Log.e("Pushy", result.toString());

                // Display error in alert
                message = ((Exception) result).getMessage();
            } else {
                message = "Pushy device token: " + result.toString() + "\n\n(copy from logcat)";
            }

            // Registration succeeded, display an alert with the device token
            new android.app.AlertDialog.Builder(this.mActivity)
                    .setTitle("Pushy")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }
}