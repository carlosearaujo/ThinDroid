package br.com.thindroid.commons.web.request;

import org.apache.http.StatusLine;

/**
 * Created by Carlos on 03/12/2015.
 */
public class ClientException extends Exception{
    ClientException(Exception exception) {
        super(exception);
    }
}
