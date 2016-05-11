package br.com.thindroid.commons.web.request;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.util.List;

/**
 * Created by Carlos on 20/08/2015.
 */
public class HttpClient {

    public void setTimeout(int connectionTimeout, int socketTimeout){
        HttpParams params = client.getParams();
        HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
        HttpConnectionParams.setConnectionTimeout(params, socketTimeout);
    }

    public HttpClient(){
        client = getThreadSafeClient();
    }

    public HttpClient(int connectionTimeout, int socketTimeout){
        client = getThreadSafeClient();
        setTimeout(connectionTimeout, socketTimeout);
    }

    protected DefaultHttpClient client;

    public String executeStringResponse(String urlRequest, HttpMethod method, List<NameValuePair> params, RequestBody requestBody) throws ServerException, ClientException {
        HttpResponse response = execute(buildRequest(urlRequest, method, params, requestBody));
        try {
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new ClientException(e);
        }
    }

    public String executeStringResponse(HttpUriRequest request) throws ServerException, ClientException {
        HttpResponse response = execute(request);
        return responseToString(response);
    }

    public String responseToString(HttpResponse response) throws ClientException {
        try {
            if(response == null){
                return null;
            }
            return EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            throw new ClientException(e);
        }
    }

    public <T> T executeJSONResponse(String urlRequest, HttpMethod method, List<NameValuePair> params, RequestBody requestBody, Class<T> clazz) throws ServerException, ClientException, IOException {
        HttpUriRequest request = buildRequest(urlRequest, method, params, requestBody);
        return executeJSONResponse(request, clazz);
    }

    public <T> T executeJSONResponse(String urlRequest, HttpMethod method, RequestBody requestBody, Class<T> clazz) throws ServerException, ClientException, IOException {
        HttpUriRequest request = buildRequest(urlRequest, method, null, requestBody);
        return executeJSONResponse(request, clazz);
    }

    public <T> T executeJSONResponse(HttpUriRequest request, Class<T> clazz) throws ServerException, ClientException, IOException {
        HttpResponse response = execute(request);
        ObjectMapper jsonConverter = new ObjectMapper();
        InputStream content = response.getEntity().getContent();
        return jsonConverter.readValue(content, clazz);
    }

    public HttpResponse execute(HttpUriRequest request) throws ServerException, ClientException {
        return handle(client, request);
    }

    public static DefaultHttpClient getThreadSafeClient()  {
        DefaultHttpClient client = new DefaultHttpClient();
        ClientConnectionManager mgr = client.getConnectionManager();
        HttpParams params = client.getParams();
        client = new DefaultHttpClient(new ThreadSafeClientConnManager(params, mgr.getSchemeRegistry()), params);
        return client;
    }

    public static HttpUriRequest buildRequest(String urlRequest, HttpMethod method) {
        return buildRequest(urlRequest, method, null, null);
    }

    public static HttpUriRequest buildRequest(String urlRequest, HttpMethod method, RequestBody requestBody) {
        return buildRequest(urlRequest, method, null, requestBody);
    }

    public static HttpUriRequest buildRequest(String urlRequest, HttpMethod method, List<NameValuePair> params, RequestBody requestBody) {
        HttpUriRequest request = null;
        if (method == HttpMethod.POST) {
            request = buildPost(urlRequest, params, requestBody);
        } else if (method == HttpMethod.GET) {
            String queryParams = params != null ? "?" + URLEncodedUtils.format(params, "utf-8") : "";
            request = new HttpGet(urlRequest + queryParams);
        }
        setContentType(requestBody, request);
        return request;
    }

    private static void setContentType(RequestBody requestBody, HttpUriRequest request) {
        if(requestBody != null) {
            if (requestBody.contentType != null) {
                request.addHeader("Content-Type", requestBody.contentType.toString());
            } else {
                request.addHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE);
            }
        }
    }

    private static HttpUriRequest buildPost(String urlRequest, List<NameValuePair> params, RequestBody requestBody) {
        HttpPost postRequest = new HttpPost(urlRequest);
        try {
            addRequestBody(requestBody, postRequest);
            addRequestParams(params, postRequest);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return postRequest;
    }

    private static void addRequestParams(List<NameValuePair> params, HttpPost postRequest) {
        if(params != null) {
            for(NameValuePair param : params){
                postRequest.getParams().setParameter(param.getName(), param.getValue());
            }
        }
    }

    private static void addRequestBody(RequestBody requestBody, HttpPost postRequest) throws UnsupportedEncodingException {
        if(requestBody != null){
            if(MediaType.APPLICATION_FORM_URLENCODED.equals(requestBody.contentType)){
                postRequest.setEntity(new UrlEncodedFormEntity(requestBody.params, "utf-8"));
            }
            else {
                postRequest.setEntity(new ByteArrayEntity(requestBody.value.getBytes("utf-8")));
            }
        }
    }


    public static HttpResponse handle(org.apache.http.client.HttpClient client, HttpUriRequest request) throws ServerException, ClientException {
        try {
            HttpResponse response = client.execute(request);
            if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
                String responseBody = EntityUtils.toString(response.getEntity());
                Log.w("HttpClient Server Error", responseBody);
                throw new ServerException(response.getStatusLine(), responseBody);
            }
            return response;
        } catch (IOException e) {
            throw new ClientException(e);
        }
    }
}
