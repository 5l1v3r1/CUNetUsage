package com.aqnichol.cunetusage;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.IOException;

/**
 * AuthenticateFragment authenticates in the background while displaying a loading dialog.
 *
 * This should only be used on an activity that implements AuthenticateFragment.Callback. Otherwise,
 * there is no way for the AuthenticateFragment to give feedback to its activity.
 */
public class AuthenticateFragment extends DialogFragment {

    private String errorMessage = null;
    private boolean hasResult = false;
    private NubbClient client = null;
    private AuthenticateTask task = null;

    /**
     * Create an AuthenticateFragment with an existing client.
     * @param client the client to use for authentication.
     * @return a client with the appropriate attributes set.
     */
    public static AuthenticateFragment create(NubbClient client) {
        AuthenticateFragment f = new AuthenticateFragment();
        Bundle b = new Bundle();
        b.putParcelable("client", client);
        f.setArguments(b);
        return f;
    }

    /**
     * Callback should be implemented by the activity to which this fragment is attached. This
     * allows the fragment to deliver events back to its activity.
     */
    public interface Callback {
        void onAuthenticationSuccess(NubbClient c);
        void onAuthenticationFailure(String error);
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        // NOTE: if the user rotates the screen, we don't want to have to start over. This allows us
        // to keep the existing AuthenticateTask.
        setRetainInstance(true);

        client = (NubbClient)getArguments().get("client");
        task = new AuthenticateTask();
        task.execute();
    }

    @Override
    public Dialog onCreateDialog(Bundle b) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.authenticating));
        return dialog;
    }

    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        if (!(a instanceof Callback)) {
            throw new RuntimeException("The owning activity must have the right callbacks.");
        }
        if (hasResult) {
            Callback callback = (Callback)a;
            if (errorMessage == null) {
                callback.onAuthenticationSuccess(client);
            } else {
                callback.onAuthenticationFailure(errorMessage);
            }
            this.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        task.cancel(false);
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        // NOTE: this seems to keep the dialog showing when the device is rotated.
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    /**
     * AuthenticateTask asynchronously authenticates with Cornell's NUBB system.
     */
    private class AuthenticateTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... v) {
            try {
                if (!client.authenticate()) {
                    return getString(R.string.login_incorrect);
                } else {
                    return null;
                }
            } catch (IOException e) {
                return e.getLocalizedMessage();
            }
        }

        @Override
        protected void onPostExecute(String error) {
            dismiss();
            if (getActivity() != null) {
                Callback c = (Callback)getActivity();
                if (error == null) {
                    c.onAuthenticationSuccess(client);
                } else {
                    c.onAuthenticationFailure(error);
                }
            } else {
                errorMessage = error;
                hasResult = true;
            }
        }
    }

}
