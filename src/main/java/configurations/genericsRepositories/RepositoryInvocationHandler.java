package configurations.genericsRepositories;

import configurations.dbas.Querys;
import configurations.orm.ConnectionPool;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RepositoryInvocationHandler implements InvocationHandler {
    private final GenericRepositoryImpl<?, ?> genericRepository;

    public RepositoryInvocationHandler(Class<?> entityClass) {
        this.genericRepository = new GenericRepositoryImpl<>(entityClass);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (method.isAnnotationPresent(Querys.class)) {
            Querys queryAnnotation = method.getAnnotation(Querys.class);
            String sql = queryAnnotation.value();

            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        stmt.setObject(i + 1, args[i]);
                    }
                }

                ResultSet rs = stmt.executeQuery();
                List<Object> results = new ArrayList<>();
                Class<?> entityClass = genericRepository.getEntityClass();

                while (rs.next()) {
                    Object entity = entityClass.getDeclaredConstructor().newInstance();
                    for (Field field : entityClass.getDeclaredFields()) {
                        field.setAccessible(true);
                        Object value = rs.getObject(field.getName());
                        field.set(entity, value);
                    }
                    results.add(entity);
                }

                return results;
            }
        }

        // Métodos normais: save, findAll, findById, etc
        return method.invoke(genericRepository, args);
    }
}
