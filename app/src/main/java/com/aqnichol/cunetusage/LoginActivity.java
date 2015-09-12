package com.aqnichol.cunetusage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
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
        progress.setTitle("Authenticating");
        progress.setMessage("Authenticating in the hackiest possible way...");

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
    public static class AuthenticateTask extends AsyncTask<NubbClient, Void, Boolean> {
        private ProgressDialog dialog;

        public AuthenticateTask(ProgressDialog d) {
            dialog = d;
        }

        @Override
        protected Boolean doInBackground(NubbClient... c) {
            try {
                return new Boolean(c[0].authenticate());
            } catch (IOException e) {
                return new Boolean(false);
            }
        }

        protected void onPostExecute(Boolean b) {
            dialog.dismiss();
        }
    }

}
