package org.java.thingdroid;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import static javax.lang.model.element.Modifier.PUBLIC;

@SupportedAnnotationTypes(value = {"org.java.thingdroid.AlarmTask"})
public class AnnotationProcessor extends AbstractProcessor {

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for(TypeElement annotation : annotations){
            TypeSpec clazzSpec = buildClass(annotation, roundEnv.getElementsAnnotatedWith(annotation));
            JavaFile javaFile = JavaFile.builder("org.java.thingdroid", clazzSpec).build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }
        return true;
    }

    private TypeSpec buildClass(TypeElement annotation, Set<? extends Element> annotatedClasses) {
        TypeSpec.Builder builder = TypeSpec.classBuilder(annotation.getSimpleName().toString() + "Resolver");
        builder.addModifiers(PUBLIC);
        builder.addSuperinterface(AnnotationResolver.class);
        MethodSpec methodSpec = MethodSpec.methodBuilder("getManagedClasses").
                addModifiers(PUBLIC).returns(Class[].class).
                addStatement(String.format("return new Class[]{%s}", annotatedClassesToStr(annotatedClasses))).
                build();
        builder.addMethod(methodSpec);
        return builder.build();
    }

    private String annotatedClassesToStr(Set<? extends Element> annotatedElements) {
        StringBuilder stringBuilder = new StringBuilder();
        Set<String> classAdded = new HashSet<>();
        for(Element element : annotatedElements){
            Element clazzElement = findEnclosingTypeElement(element);
            if(!classAdded.contains(clazzElement.toString())) {
                stringBuilder.append(clazzElement.toString() + ".class,");
                classAdded.add(element.toString());
            }
        }
        return stringBuilder.toString().substring(0, stringBuilder.length() - 1);
    }

    public static TypeElement findEnclosingTypeElement( Element e ){
        while( e != null && !(e instanceof TypeElement) ){
            e = e.getEnclosingElement();
        }
        return TypeElement.class.cast(e);

    }
}
