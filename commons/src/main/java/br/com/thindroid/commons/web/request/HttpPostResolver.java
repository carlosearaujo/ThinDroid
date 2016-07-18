package br.com.thindroid.commons.web.request;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.springframework.http.HttpMethod;

/**
 * Created by carlos on 18/07/16.
 */
public class HttpPostResolver extends HttpEntityEnclosingResolver {
    @Override
    HttpEntityEnclosingRequestBase getHttpEntityEnclosingRequestBase(String urlRequest) {
        return new HttpPost(urlRequest);
    }

    @Override
    public boolean handleMethod(HttpMethod method) {
        return HttpMethod.POST.equals(method);
    }
}
