package br.com.thindroid.commons.web.request;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.http.HttpMethod;

import java.util.List;

/**
 * Created by carlos on 18/07/16.
 */
public interface HttpMethodResolver {
    boolean handleMethod(HttpMethod method);
    HttpUriRequest buildRequest(String urlRequest, List<NameValuePair> params, RequestBody requestBody);
}
