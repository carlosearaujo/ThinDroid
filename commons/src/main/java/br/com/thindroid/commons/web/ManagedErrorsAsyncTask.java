package br.com.thindroid.commons.web;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

/**
 * Created by Carlos on 26/05/2014.
 */
public abstract class ManagedErrorsAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    protected Context mContext;
    private boolean showErrorInDialog;
    private DialogInterface.OnClickListener onMessageClosedListener;

    public  ManagedErrorsAsyncTask(Context context, boolean showResultMessageInDialog){
        mContext = context;
        this.showErrorInDialog = showResultMessageInDialog;
    }

    protected String message;
    protected String messageTitle = "Erro";

    protected void setOnMessageClosedAction(DialogInterface.OnClickListener onMessageClosedListener){
        this.onMessageClosedListener = onMessageClosedListener;
    }

    @Override
    protected void onPostExecute(Result o) {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if (this.message != null) {
                if (showErrorInDialog) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                    dialog.setTitle(messageTitle);
                    dialog.setMessage(message);
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("OK", onMessageClosedListener);
                    dialog.show();
                } else {
                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                }
            }
            onMessageClosedListener = null;
            this.message = null;
            this.messageTitle = "Erro";
        }
        catch (Exception ex){}
    }

    protected ProgressDialog progressDialog;

    protected void callDialog(String message){
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
}
