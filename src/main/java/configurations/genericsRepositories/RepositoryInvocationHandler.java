package configurations.genericsRepositories;

import configurations.dbas.Id;
import configurations.dbas.OneToOne;
import configurations.dbas.Querys;
import configurations.orm.ConnectionPool;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
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
        // Verifica se o método tem a anotação Querys, indicando uma consulta personalizada
        if (method.isAnnotationPresent(Querys.class)) {
            Querys queryAnnotation = method.getAnnotation(Querys.class); // Obtém a consulta SQL da anotação
            String sql = queryAnnotation.value(); // Obtém o SQL da anotação Querys

            System.out.println("Consulta personalizada detectada: " + sql); // Debug: exibe a consulta

            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                // Verifica se existem parâmetros e os configura na consulta
                if (args != null) {
                    System.out.println("Número de parâmetros: " + args.length); // Debug: número de parâmetros passados
                    for (int i = 0; i < args.length; i++) {
                        stmt.setObject(i + 1, args[i]);
                        System.out.println("Parâmetro " + (i + 1) + " configurado: " + args[i]); // Debug: valor de cada parâmetro
                    }
                }

                // Executa a consulta e processa os resultados
                ResultSet rs = stmt.executeQuery();
                List<Object> results = new ArrayList<>();
                Class<?> entityClass = genericRepository.getEntityClass(); // Classe da entidade

                System.out.println("Processando os resultados da consulta..."); // Debug: indicando que os resultados estão sendo processados

                // Mapeia os resultados para objetos da entidade
                while (rs.next()) {
                    Object entity = entityClass.getDeclaredConstructor().newInstance(); // Cria uma instância da entidade
                    System.out.println("Criada nova instância da entidade: " + entityClass.getSimpleName()); // Debug: nome da classe da entidade

                    // Atribui os valores dos campos do ResultSet para os campos da entidade
                    for (Field field : entityClass.getDeclaredFields()) {
                        field.setAccessible(true);

                        // Verifica se o campo é uma associação com outra entidade
                        if (field.isAnnotationPresent(OneToOne.class)) {
                            // Supondo convenção: nome do campo + "_id"
                            String foreignKeyColumn = field.getName() + "_id";
                            Object foreignKeyValue = rs.getObject(foreignKeyColumn);
                            System.out.println("Atribuindo valor ao campo relacional '" + field.getName() + "': " + foreignKeyValue); // Debug

                            if (foreignKeyValue != null) {
                                // Cria instância da entidade relacionada
                                Class<?> relatedClass = field.getType();
                                Object relatedEntity = relatedClass.getDeclaredConstructor().newInstance();

                                // Encontra o campo anotado com @Id na entidade relacionada
                                Field idField = Arrays.stream(relatedClass.getDeclaredFields())
                                        .filter(f -> f.isAnnotationPresent(Id.class))
                                        .findFirst()
                                        .orElseThrow(() -> new RuntimeException("Campo @Id não encontrado em " + relatedClass.getSimpleName()));

                                idField.setAccessible(true);
                                idField.set(relatedEntity, foreignKeyValue); // Atribui o ID
                                field.set(entity, relatedEntity); // Atribui o objeto relacionado
                            }
                        } else {
                            // Campo comum
                            Object value = rs.getObject(field.getName());
                            System.out.println("Atribuindo valor ao campo '" + field.getName() + "': " + value); // Debug: atribuição de valor
                            field.set(entity, value);
                        }
                    }

                    results.add(entity); // Adiciona a entidade mapeada à lista de resultados
                }

                // Retorna a lista de entidades mapeadas
                System.out.println("Consulta executada com sucesso. Resultados mapeados: " + results.size() + " entidades."); // Debug: número de resultados
                return results;
            } catch (SQLException e) {
                System.err.println("Erro ao executar consulta SQL: " + e.getMessage()); // Debug: captura de exceção SQL
                throw e;
            } catch (Exception e) {
                System.err.println("Erro ao processar a consulta: " + e.getMessage()); // Debug: captura de outras exceções
                throw e;
            }
        }
        // Para métodos normais como save, findAll, etc., delega a execução para o repositório genérico
        System.out.println("Método normal chamado: " + method.getName()); // Debug: método normal
        return method.invoke(genericRepository, args);
    }
}