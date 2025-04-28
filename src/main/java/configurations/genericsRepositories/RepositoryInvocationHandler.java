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


            try (Connection conn = ConnectionPool.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                // Configura os parâmetros da consulta SQL, se houver
                if (args != null) {
                    for (int i = 0; i < args.length; i++) {
                        stmt.setObject(i + 1, args[i]);
                    }
                }

                // Executa a consulta e processa os resultados
                ResultSet rs = stmt.executeQuery();
                List<Object> results = new ArrayList<>();
                Class<?> entityClass = genericRepository.getEntityClass(); // Classe da entidade

                // Mapeia os resultados para objetos da entidade
                while (rs.next()) {
                    Object entity = entityClass.getDeclaredConstructor().newInstance(); // Cria uma instância da entidade
                    // Atribui os valores dos campos do ResultSet para os campos da entidade
                    for (Field field : entityClass.getDeclaredFields()) {
                        field.setAccessible(true);
                        Object value = rs.getObject(field.getName());
                        field.set(entity, value);
                    }
                    results.add(entity); // Adiciona a entidade mapeada à lista de resultados
                }

                // Retorna a lista de entidades mapeadas
                return results;
            }
        }

        // Para métodos normais como save, findAll, etc., delega a execução para o repositório genérico
        return method.invoke(genericRepository, args);
    }
}
