package br.com.thindroid;

import java.lang.annotation.Annotation;

/**
 * Created by Carlos on 23/12/2015.
 */
public abstract class AnnotationResolver {

    public static AnnotationResolver getResolver(Class<? extends Annotation> annotation){
        try {
            Class<AnnotationResolver> schedulerResolver = (Class<AnnotationResolver>) Class.forName(annotation.getName() + "Resolver");
            AnnotationResolver scheduleResolverInstance =  schedulerResolver.newInstance();
            return scheduleResolverInstance;
        }
        catch (ClassNotFoundException ex){
            return new DefaultResolver();
        }
        catch (Exception ex){
            throw new RuntimeException("Error when load " + annotation.toString(), ex);
        }
    }

    public abstract <T> T[] getManagedElements();
}
