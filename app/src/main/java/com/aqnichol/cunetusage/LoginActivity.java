package com.aqnichol.cunetusage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;


public class LoginActivity extends Activity {

    private EditText netIdField;
    private EditText passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        netIdField = (EditText)findViewById(R.id.net_id);
        passwordField = (EditText)findViewById(R.id.password);

        NubbClient oldClient = new SessionSaver(this).loadClient();
        if (oldClient != null) {
            new AuthenticateTask().execute(oldClient);
        } else {
            netIdField.requestFocus();
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    login(null);
                    return true;
                }
                return false;
            }
        });
    }

    public void login(View buttonView) {
        NubbClient client = new NubbClient();
        client.setUsername(netIdField.getText().toString());
        client.setPassword(passwordField.getText().toString());
        new AuthenticateTask().execute(client);
    }

    /**
     * AuthenticateTask asynchronously authenticates with Cornell's NUBB system.
     */
    private class AuthenticateTask extends AsyncTask<NubbClient, Void, Boolean> {
        private ProgressDialog dialog;
        private NubbClient client;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(LoginActivity.this);
            dialog.setMessage(getString(R.string.authenticating));
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(false);
                }
            });
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(NubbClient... c) {
            client = c[0];
            try {
                return new Boolean(client.authenticate());
            } catch (IOException e) {
                return new Boolean(false);
            }
        }

        @Override
        protected void onPostExecute(Boolean b) {
            dialog.dismiss();
            SessionSaver saver = new SessionSaver(LoginActivity.this);
            if (b) {
                saver.saveClient(client);
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                i.putExtra("client", client);
                startActivity(i);
            } else {
                // NOTE: we clear the saved session now because the login could have been from a
                // saved session. In this case, it would not be cool if the failed login happened
                // every time the user opened the app.
                saver.clear();

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage(R.string.login_error);
                builder.setPositiveButton(R.string.ok, null);
                builder.setCancelable(false);
                builder.create().show();
            }
        }
    }

}
