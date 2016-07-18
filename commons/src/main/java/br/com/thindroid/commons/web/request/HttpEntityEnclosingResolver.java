package br.com.thindroid.commons.web.request;

import android.support.annotation.NonNull;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by carlos on 18/07/16.
 */
public abstract class HttpEntityEnclosingResolver implements HttpMethodResolver {

    abstract HttpEntityEnclosingRequestBase getHttpEntityEnclosingRequestBase(String urlRequest);

    @Override
    public HttpUriRequest buildRequest(String urlRequest, List<NameValuePair> params, RequestBody requestBody) {
        HttpEntityEnclosingRequestBase postRequest = getHttpEntityEnclosingRequestBase(urlRequest);
        try {
            addRequestBody(requestBody, postRequest);
            addRequestParams(params, postRequest);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return postRequest;
    }

    private static void addRequestParams(List<NameValuePair> params, HttpEntityEnclosingRequestBase postRequest) {
        if(params != null) {
            for(NameValuePair param : params){
                postRequest.getParams().setParameter(param.getName(), param.getValue());
            }
        }
    }

    private static void addRequestBody(RequestBody requestBody, HttpEntityEnclosingRequestBase postRequest) throws UnsupportedEncodingException {
        if(requestBody != null){
            if(MediaType.APPLICATION_FORM_URLENCODED.equals(requestBody.contentType)){
                postRequest.setEntity(new UrlEncodedFormEntity(requestBody.params, "utf-8"));
            }
            else {
                postRequest.setEntity(new ByteArrayEntity(requestBody.value.getBytes("utf-8")));
            }
        }
    }
}
