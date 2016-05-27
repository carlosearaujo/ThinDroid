package br.com.thindroid.commons.web.request;

import org.apache.http.NameValuePair;
import org.springframework.http.MediaType;

import java.util.List;

/**
 * Created by Carlos on 01/09/2015.
 */
public class RequestBody {

    public RequestBody(String value, MediaType contentType) {
        if(MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(contentType)){
            throw new IllegalArgumentException("ContentType APPLICATION_FORM_URLENCODED_VALUE need list of NameValuePair. See construtor with NameValuePair");
        }
        this.value = value;
        this.contentType = contentType;
    }

    public RequestBody(List<NameValuePair> params, MediaType contentType){
        this.params = params;
        this.contentType = contentType;
    }

    String value;
    MediaType contentType;
    List<NameValuePair> params;

}
