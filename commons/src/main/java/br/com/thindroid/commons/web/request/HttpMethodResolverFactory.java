package br.com.thindroid.commons.web.request;

import org.springframework.http.HttpMethod;

/**
 * Created by carlos on 18/07/16.
 */
public class HttpMethodResolverFactory {
    public static HttpMethodResolver[] getAllAvaiables() {
        return new HttpMethodResolver[]{new HttpPostResolver(), new HttpPutResolver(), new HttpGetResolver()};
    }

    public HttpMethodResolver getHttpMethodResolver(HttpMethod method) {
        HttpMethodResolver[] resolvers = HttpMethodResolverFactory.getAllAvaiables();
        for(HttpMethodResolver resolver : resolvers){
            if(resolver.handleMethod(method)){
                return resolver;
            }
        }
        throw new UnsupportedOperationException(String.format("Unsupported HttpMethod (%s)", method.toString()));
    }
}
