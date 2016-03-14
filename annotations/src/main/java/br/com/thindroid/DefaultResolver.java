package br.com.thindroid;

/**
 * Created by Carlos on 11/03/2016.
 */
public class DefaultResolver extends AnnotationResolver {
    @Override
    public Class[] getManagedClasses() {
        return new Class[0];
    }
}
