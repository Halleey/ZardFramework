package configurations.scanners;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.Set;
public class ClassScanner {

    public static Set<Class<?>> getAnnotatedClasses(String basePackage, Class<?> annotationClass) {
        Reflections reflections = new Reflections(basePackage);

//
//        //System.out.println(" Classes anotadas com @" + annotationClass.getSimpleName() + " em '" + basePackage + "':");
//        for (Class<?> clazz : annotatedClasses) {
//         //   System.out.println(" ->  " + clazz.getName());
//        }

        return reflections.getTypesAnnotatedWith((Class<? extends Annotation>) annotationClass);
    }
}