package br.com.thindroid.commons.web.request;

import android.util.Log;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

import br.com.thindroid.commons.utils.IOUtils;

/**
 * Created by carlos.araujo on 05/02/2015.
 */
public class IgnoreErrorsResponseHandler implements ResponseErrorHandler{

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return false;
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        Log.w("ErrorsResponseHandler", IOUtils.convertStreamToString(response.getBody()));
    }
}
