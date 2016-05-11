package br.com.thindroid.commons.web.request;

import org.apache.http.StatusLine;

/**
 * Created by Carlos on 21/08/2015.
 */
public class ServerException extends Exception {

    private int statusCode;
    private String errorBody;

    ServerException(StatusLine statusLine, String body) {
        super(statusLine.getReasonPhrase());
        statusCode = statusLine.getStatusCode();
        this.errorBody = body;
    }

    public int getStatusCode(){
        return statusCode;
    }

    public String getErrorBody(){
        return this.errorBody;
    }
}
