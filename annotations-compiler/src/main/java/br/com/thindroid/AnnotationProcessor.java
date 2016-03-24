package br.com.thindroid;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
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

import br.com.thindroid.annotations.AnnotationResolver;

import static javax.lang.model.element.Modifier.PUBLIC;

@SupportedAnnotationTypes(value = {"br.com.thindroid.annotations.AlarmTask", "br.com.thindroid.annotations.Repository"})
public class AnnotationProcessor extends AbstractProcessor {

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for(TypeElement annotation : annotations){
            TypeSpec clazzSpec = buildClass(annotation, roundEnv.getElementsAnnotatedWith(annotation));
            JavaFile javaFile = JavaFile.builder("br.com.thindroid.annotations", clazzSpec).build();
            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            }
        }
        return true;
    }

    private TypeSpec buildClass(TypeElement annotation, Set<? extends Element> annotatedClasses) {
        ElementType target = getAnnotationTarget(annotation);
        TypeSpec.Builder builder = TypeSpec.classBuilder(annotation.getSimpleName().toString() + "Resolver");
        builder.addModifiers(PUBLIC);
        builder.superclass(AnnotationResolver.class);
        MethodSpec methodSpec = MethodSpec.methodBuilder("getManagedElements").
                addModifiers(PUBLIC).returns(target.equals(ElementType.TYPE) ? Class[].class : Method[].class).
                addCode("try{").
                addStatement(String.format("return new %s{%s}", target.equals(ElementType.TYPE) ? "Class[]" : "Method[]" , buildReturn(target, annotatedClasses))).
                addStatement("}catch(Exception ex){throw new RuntimeException(ex)").
                addCode("}").
                build();
        builder.addMethod(methodSpec);
        return builder.build();
    }

    private String buildReturn(ElementType target, Set<? extends Element> annotatedClasses) {
        if(target.equals(ElementType.TYPE)){
            return buildTypeReturn(annotatedClasses);
        }
        else{
            return buildMethodReturn(annotatedClasses);
        }
    }

    private String buildMethodReturn(Set<? extends Element> annotatedClasses) {
        StringBuilder stringBuilder = new StringBuilder();
        for(Element element : annotatedClasses){
            Element clazzElement = findEnclosingTypeElement(element);
            stringBuilder.append(String.format(clazzElement.toString() + ".class.getMethod(\"%s\"),", element.getSimpleName().toString()));
        }
        return stringBuilder.toString().substring(0, stringBuilder.length() - 1);
    }

    private String buildTypeReturn(Set<? extends Element> annotatedElements) {
        StringBuilder stringBuilder = new StringBuilder();
        Set<String> classAdded = new HashSet<>();
        for(Element element : annotatedElements){
            Element clazzElement = findEnclosingTypeElement(element);
            if(!classAdded.contains(clazzElement.toString())) {
                stringBuilder.append(clazzElement.toString() + ".class,");
                classAdded.add(clazzElement.toString());
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

    public ElementType getAnnotationTarget(TypeElement annotation) {
        return annotation.getAnnotation(Target.class).value()[0];
    }
}
