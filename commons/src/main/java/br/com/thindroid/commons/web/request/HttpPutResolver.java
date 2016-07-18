package br.com.thindroid.commons.web.request;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPut;
import org.springframework.http.HttpMethod;

/**
 * Created by carlos on 18/07/16.
 */
public class HttpPutResolver extends HttpEntityEnclosingResolver {
    @Override
    HttpEntityEnclosingRequestBase getHttpEntityEnclosingRequestBase(String urlRequest) {
        return new HttpPut(urlRequest);
    }

    @Override
    public boolean handleMethod(HttpMethod method) {
        return HttpMethod.PUT.equals(method);
    }
}
