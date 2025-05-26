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
    private final GenericRepositoryImpl<?, ?> genericRepository;

    public RepositoryInvocationHandler(Class<?> entityClass) {
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

                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        stmt.setObject(i + 1, args[i]);
                        System.out.println("Parâmetro " + (i + 1) + " configurado: " + args[i]);
                    }
                }

                ResultSet rs = stmt.executeQuery();
                List<Object> results = new ArrayList<>();

                Class<?> resultClass = getReturnTypeClass(method);
                System.out.println("Tipo de retorno esperado: " + resultClass.getSimpleName());

                while (rs.next()) {
                    Object instance = resultClass.getDeclaredConstructor().newInstance();

                    for (Field field : resultClass.getDeclaredFields()) {
                        field.setAccessible(true);
                        String columnName = field.getName();

                        try {
                            if (field.isAnnotationPresent(OneToOne.class)) {
                                String foreignKeyColumn = columnName + "_id";
                                Object foreignKeyValue = rs.getObject(foreignKeyColumn);

                                System.out.println("Campo relacional detectado: " + columnName + " -> FK: " + foreignKeyColumn);

                                if (foreignKeyValue != null) {
                                    Class<?> relatedClass = field.getType();
                                    Object relatedInstance = relatedClass.getDeclaredConstructor().newInstance();

                                    Field idField = Arrays.stream(relatedClass.getDeclaredFields())
                                            .filter(f -> f.isAnnotationPresent(Id.class))
                                            .findFirst()
                                            .orElseThrow(() -> new RuntimeException("Campo @Id não encontrado em " + relatedClass.getSimpleName()));

                                    idField.setAccessible(true);
                                    idField.set(relatedInstance, foreignKeyValue);
                                    field.set(instance, relatedInstance);
                                    System.out.println("Relacionamento " + columnName + " preenchido com ID: " + foreignKeyValue);
                                }

                            } else {
                                Object value = rs.getObject(columnName);
                                field.set(instance, value);
                                System.out.println("Campo '" + columnName + "' preenchido com: " + value);
                            }

                        } catch (SQLException sqlEx) {
                            System.out.println("Coluna '" + columnName + "' não encontrada no ResultSet. Ignorando.");
                        } catch (Exception ex) {
                            System.err.println("Erro ao atribuir valor ao campo '" + columnName + "': " + ex.getMessage());
                            throw ex;
                        }
                    }

                    results.add(instance);
                }

                System.out.println("Consulta executada com sucesso. Total de entidades mapeadas: " + results.size());

                // 🔁 Ajuste do retorno com base na assinatura do método
                if (List.class.isAssignableFrom(method.getReturnType())) {
                    return results;
                } else if (!results.isEmpty()) {
                    return results.get(0); // Apenas o primeiro resultado
                } else {
                    return null;
                }

            } catch (SQLException e) {
                System.err.println("Erro SQL: " + e.getMessage());
                throw e;
            } catch (Exception e) {
                System.err.println("Erro ao processar a entidade de retorno: " + e.getMessage());
                throw e;
            }
        }

        // Chamadas padrão (ex: save, findAll)
        Method targetMethod = genericRepository.getClass().getMethod(method.getName(), method.getParameterTypes());
        return targetMethod.invoke(genericRepository, args);
    }

    private Class<?> getReturnTypeClass(Method method) {
        Type genericReturnType = method.getGenericReturnType();

        if (genericReturnType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericReturnType;
            Type[] actualTypes = pt.getActualTypeArguments();
            if (actualTypes.length == 1 && actualTypes[0] instanceof Class) {
                return (Class<?>) actualTypes[0]; // Ex: List<EntidadeOuDTO>
            }
        }

        // Default: assume a classe da entidade
        return genericRepository.getEntityClass();
    }
}
