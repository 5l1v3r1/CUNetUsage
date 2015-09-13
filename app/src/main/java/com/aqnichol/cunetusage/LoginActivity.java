package com.aqnichol.cunetusage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

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
    }

    public void login(View buttonView) {
        NubbClient client = new NubbClient();
        client.setUsername(netIdField.getText().toString());
        client.setPassword(passwordField.getText().toString());

        ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle(R.string.authenticating);

        final AuthenticateTask task = new AuthenticateTask(progress);

        progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                task.cancel(false);
            }
        });

        progress.show();
        task.execute(client);
    }

    /**
     * AuthenticateTask asynchronously authenticates with Cornell's NUBB system.
     */
    private class AuthenticateTask extends AsyncTask<NubbClient, Void, Boolean> {
        private ProgressDialog dialog;
        private NubbClient client;

        public AuthenticateTask(ProgressDialog d) {
            dialog = d;
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

        protected void onPostExecute(Boolean b) {
            dialog.dismiss();
            if (b.booleanValue()) {
                MainActivity.currentClient = client;
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(i);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setMessage(R.string.login_error);
                builder.setPositiveButton(R.string.ok, null);
                builder.create().show();
            }
        }
    }

}
