package configurations.scanners;

import configurations.genericsRepositories.RepositoryFactory;
import configurations.instancias.Repository;
import configurations.instancias.RestController;
import configurations.instancias.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.*;
public class ZardContext {

    private final Map<Class<?>, Object> container = new HashMap<>();

    public void initialize(String basePackage) throws Exception {
        // Repositórios
        for (Class<?> clazz : ClassScanner.getAnnotatedClasses(basePackage, Repository.class)) {
            ParameterizedType genericInterface = (ParameterizedType) clazz.getGenericInterfaces()[0];
            Class<?> entityClass = (Class<?>) genericInterface.getActualTypeArguments()[0];
            Object instance = RepositoryFactory.createRepository(clazz, entityClass);
            container.put(clazz, instance);
        }

        // Serviços
        for (Class<?> clazz : ClassScanner.getAnnotatedClasses(basePackage, Service.class)) {
            instantiateWithDependencies(clazz);
        }

        // Controladores
        for (Class<?> clazz : ClassScanner.getAnnotatedClasses(basePackage, RestController.class)) {
            instantiateWithDependencies(clazz);
        }
    }

    private void instantiateWithDependencies(Class<?> clazz) throws Exception {
        Constructor<?> constructor = clazz.getConstructors()[0];
        Object[] params = Arrays.stream(constructor.getParameterTypes())
                .map(container::get)
                .toArray();
        Object instance = constructor.newInstance(params);
        container.put(clazz, instance);
    }

    public <T> T get(Class<T> clazz) {
        return clazz.cast(container.get(clazz));
    }

    public Collection<Object> getControllers() {
        return container.entrySet().stream()
                .filter(e -> e.getKey().isAnnotationPresent(RestController.class))
                .map(Map.Entry::getValue)
                .toList();
    }
}

