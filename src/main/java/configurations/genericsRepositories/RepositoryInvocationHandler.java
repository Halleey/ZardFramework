package configurations.genericsRepositories;

import configurations.dbas.Id;
import configurations.dbas.OneToOne;
import configurations.dbas.Querys;
import configurations.orm.ConnectionPool;

import java.lang.reflect.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RepositoryInvocationHandler implements InvocationHandler {
    private final GenericRepositoryImpl<?, ?> genericRepository; // Instância do repositório genérico

    public RepositoryInvocationHandler(Class<?> entityClass) {
        // Cria uma instância do repositório genérico com a classe da entidade
        this.genericRepository = new GenericRepositoryImpl<>(entityClass);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(Querys.class)) {
            Querys queryAnnotation = method.getAnnotation(Querys.class);
            String sql = queryAnnotation.value();
            System.out.println("Consulta personalizada detectada: " + sql);

            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                // Define os parâmetros da query
                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        stmt.setObject(i + 1, args[i]);
                        System.out.println("Parâmetro " + (i + 1) + " configurado: " + args[i]);
                    }
                }

                ResultSet rs = stmt.executeQuery();
                List<Object> results = new ArrayList<>();

                // Aqui detectamos o tipo de retorno do método
                Class<?> returnType = getReturnTypeClass(method);

                System.out.println("Tipo de retorno esperado: " + returnType.getSimpleName());

                while (rs.next()) {
                    Object resultObject = returnType.getDeclaredConstructor().newInstance();

                    for (Field field : returnType.getDeclaredFields()) {
                        field.setAccessible(true);
                        String columnName = field.getName(); // Nome padrão

                        try {
                            Object value = rs.getObject(columnName);
                            System.out.println("Atribuindo campo '" + columnName + "' com valor: " + value);
                            field.set(resultObject, value);
                        } catch (SQLException e) {
                            System.out.println("Coluna '" + columnName + "' não encontrada no ResultSet. Ignorando.");
                        }
                    }

                    results.add(resultObject);
                }

                return results;
            } catch (SQLException e) {
                System.err.println("Erro SQL: " + e.getMessage());
                throw e;
            } catch (Exception e) {
                System.err.println("Erro ao instanciar classe de retorno: " + e.getMessage());
                throw e;
            }
        }

        // Métodos normais delegados ao repositório genérico
        return method.invoke(genericRepository, args);
    }

    private Class<?> getReturnTypeClass(Method method) {
        Type genericReturnType = method.getGenericReturnType();

        if (genericReturnType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericReturnType;
            Type[] actualTypes = pt.getActualTypeArguments();
            if (actualTypes.length == 1 && actualTypes[0] instanceof Class) {
                return (Class<?>) actualTypes[0]; // Ex: List<MeuDTO>
            }
        }

        // Se não for parametrizado, assume a classe da entidade
        return genericRepository.getEntityClass();
    }

}