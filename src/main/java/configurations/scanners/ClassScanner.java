package configurations.scanners;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Set;
public class ClassScanner {

    public static Set<Class<?>> getAnnotatedClasses(String basePackage, Class<?> annotationClass) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith((Class<? extends Annotation>) annotationClass);

        // debugando para ver se o reflection ta acessando o pacote corretamente
      //  System.out.println(" Classes anotadas com @" + annotationClass.getSimpleName() + " em '" + basePackage + "':");
//        for (Class<?> clazz : annotatedClasses) {
//            System.out.println(" ->  " + clazz.getName());
//        }

        return annotatedClasses;
    }
}