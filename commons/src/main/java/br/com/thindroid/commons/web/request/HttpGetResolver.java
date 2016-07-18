package br.com.thindroid.commons.web.request;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.http.HttpMethod;

import java.util.List;

/**
 * Created by carlos on 18/07/16.
 */
public class HttpGetResolver implements HttpMethodResolver {
    @Override
    public boolean handleMethod(HttpMethod method) {
        return HttpMethod.GET.equals(method);
    }

    @Override
    public HttpUriRequest buildRequest(String urlRequest, List<NameValuePair> params, RequestBody requestBody) {
        String queryParams = params != null ? "?" + URLEncodedUtils.format(params, "utf-8") : "";
        return  new HttpGet(urlRequest + queryParams);
    }
}
