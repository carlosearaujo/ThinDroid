package br.com.thindroid.commons.web.request;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.apache.http.conn.ConnectTimeoutException;

import br.com.thindroid.commons.Application;
import br.com.thindroid.commons.R;
import br.com.thindroid.commons.utils.ExceptionUtils;

/**
 * Created by Carlos on 20/08/2015.
 */
public class NetworkExceptionHandler {

    private Context context;

    public NetworkExceptionHandler(){
        context = Application.getContext();
    }

    public String defaultHandler(Exception exception) {
        if(exception instanceof ServerException){
            return defaultServerExceptionHandler((ServerException) exception);
        }
        else if(exception instanceof ClientException){
            return defaultClientExceptionHandler((ClientException) exception);
        }
        else{
            logUnknown(exception);
            throw new RuntimeException(exception);
        }
    }

    public String defaultServerExceptionHandler(ServerException exception){
        if(exception.getStatusCode() == HttpStatus.SC_UNAUTHORIZED){
            return context.getString(R.string.default_unauthorized_error);
        }
        else{
            return context.getResources().getString(R.string.default_unavailable_server_error);
        }
    }

    public String defaultClientExceptionHandler(ClientException exception){
        if(ExceptionUtils.isCause(ConnectTimeoutException.class, exception)){
            return context.getResources().getString(R.string.timeout_error);
        }
        return context.getResources().getString(R.string.default_client_connection_error);
    }

    private static void logUnknown(Exception ex) {
        Log.w("WebServiceRequest", "Erro inesperado", ex);
    }
}
