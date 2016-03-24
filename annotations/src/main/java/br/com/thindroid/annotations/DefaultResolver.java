package br.com.thindroid.annotations;

import br.com.thindroid.annotations.AnnotationResolver;

/**
 * Created by Carlos on 11/03/2016.
 */
public class DefaultResolver extends AnnotationResolver {
    @Override
    public Object[] getManagedElements() {
        return new Object[0];
    }
}
