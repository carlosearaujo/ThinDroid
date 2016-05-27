package br.com.thindroid.commons.web.request;

/**
 * Created by Carlos on 20/08/2015.
 */
public class UnknownHttpException extends RuntimeException {

    public UnknownHttpException(){

    }

    public UnknownHttpException(Exception ex){
        super(ex);
    }
}
