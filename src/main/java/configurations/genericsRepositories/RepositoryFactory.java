package configurations.genericsRepositories;

import java.lang.reflect.Proxy;

public class RepositoryFactory {

    @SuppressWarnings("unchecked")
    public static <T> T createRepository(Class<T> repositoryInterface, Class<?> entityClass) {
        return (T) Proxy.newProxyInstance(
                repositoryInterface.getClassLoader(),
                new Class<?>[]{repositoryInterface},
                new RepositoryInvocationHandler(entityClass)
        );
    }
}
