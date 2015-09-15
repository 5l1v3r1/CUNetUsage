package com.aqnichol.cunetusage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;


public class LoginActivity extends Activity implements AuthenticateFragment.Callback {

    private EditText netIdField;
    private EditText passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        netIdField = (EditText)findViewById(R.id.net_id);
        passwordField = (EditText)findViewById(R.id.password);

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

        if (!isAuthenticating()) {
            NubbClient oldClient = new SessionSaver(this).loadClient();
            if (oldClient != null) {
                authenticate(oldClient);
            } else {
                netIdField.requestFocus();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
        }
    }

    /**
     * Login is triggered when the user hits requests a login through the UI.
     * @param buttonView the button that triggered this event, or null if no button triggered it.
     */
    public void login(View buttonView) {
        NubbClient client = new NubbClient();
        client.setUsername(netIdField.getText().toString());
        client.setPassword(passwordField.getText().toString());
        authenticate(client);
    }

    @Override
    public void onAuthenticationSuccess(NubbClient client) {
        setFieldsEnabled(true);

        SessionSaver saver = new SessionSaver(LoginActivity.this);
        saver.saveClient(client);

        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        i.putExtra("client", client);
        startActivity(i);
    }

    @Override
    public void onAuthenticationFailure(String error) {
        // NOTE: we clear the saved session now because the login could have been from a
        // saved session. In this case, it would not be cool if the failed login happened
        // every time the user opened the app.
        SessionSaver saver = new SessionSaver(LoginActivity.this);
        saver.clear();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.login_error));
        builder.setMessage(error);
        builder.setPositiveButton(R.string.ok, null);
        builder.setCancelable(false);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                setFieldsEnabled(true);
            }
        });
        builder.create().show();
    }

    private void authenticate(NubbClient c) {
        // NOTE: if we don't disable the fields, then the following scenario can happen. If the user
        // is in portrait mode, they can switch to landscape mode while the dialog is open. When
        // this happens, the EditText they had selected may choose to go into full screen mode (i.e.
        // extracted UI mode). When this happens, for some reason the dialog becomes invisible.
        setFieldsEnabled(false);

        AuthenticateFragment.create(c).show(getFragmentManager(), "authenticate");
    }

    private boolean isAuthenticating() {
        return getFragmentManager().findFragmentByTag("authenticate") != null;
    }

    private void setFieldsEnabled(boolean flag) {
        netIdField.setEnabled(flag);
        passwordField.setEnabled(flag);
    }

}
