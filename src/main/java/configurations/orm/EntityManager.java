package configurations.orm;


import configurations.dbas.Column;
import configurations.dbas.Entity;
import configurations.dbas.Id;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EntityManager {

    // Método que gera o schema de todas as classes com a anotação @Entity
    public static void generateSchema(String basePackage) {
        // Encontrando todas as classes com a anotação @Entity
        List<Class<?>> entities = getEntityClasses(basePackage);

        // Gerando a tabela para cada classe de entidade encontrada
        for (Class<?> entityClass : entities) {
            if (entityClass.isAnnotationPresent(Entity.class)) {
                generateTable(entityClass);
            }
        }
    }

    // Método que encontra todas as classes com a anotação @Entity em um pacote
    private static List<Class<?>> getEntityClasses(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        System.out.println("Encontrado tabela com anotações @entity");
        // Encontrando todas as classes com a anotação @Entity
        Set<Class<?>> entityClasses = reflections.getTypesAnnotatedWith(Entity.class);

        return new ArrayList<>(entityClasses);
    }

    // Método que gera a tabela para uma classe de entidade
    private static void generateTable(Class<?> entityClass) {
        // Pega o nome da tabela (usando o nome da classe ou o valor da anotação @Entity)
        Entity entity = entityClass.getAnnotation(Entity.class);
        String tableName = entity.value().isEmpty() ? entityClass.getSimpleName().toLowerCase() : entity.value();

        // Comando SQL para criar a tabela
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");

        List<String> columnDefinitions = getStrings(entityClass);

        // Adiciona as definições das colunas na consulta SQL
        sql.append(String.join(", ", columnDefinitions));
        sql.append(");");

        System.out.println("SQL Gerado para a criação da tabela: " + sql.toString());  // Aqui vamos ver o SQL gerado

        // Agora, vamos verificar a execução do SQL
        try (Connection conn = ConnectionPool.getConnection();
             Statement stmt = conn.createStatement()) {
            System.out.println("Executando SQL: " + sql.toString());
            stmt.execute(sql.toString());  // Verificando se o SQL é executado sem problemas
            System.out.println("Tabela criada com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao executar SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<String> getStrings(Class<?> entityClass) {
        Field[] fields = entityClass.getDeclaredFields();
        List<String> columnDefinitions = new ArrayList<>();

        // Processa cada campo da classe
        for (Field field : fields) {
            String columnDefinition = getString(field);

            if (field.isAnnotationPresent(Id.class)) {
                // Aqui estamos verificando se o campo é a chave primária e deve ser auto incremento
                columnDefinition += " PRIMARY KEY";

                // Verifica se a anotação @Id implica em auto incremento (considerando MySQL ou similar)
                columnDefinition += " AUTO_INCREMENT";
            }

            columnDefinitions.add(columnDefinition);
        }
        return columnDefinitions;
    }

    private static String getString(Field field) {
        String columnName = field.getName();
        String columnType = mapJavaTypeToSQL(field.getType());

        // Verifica se o campo tem a anotação @Column
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            if (!column.value().isEmpty()) {
                columnName = column.value();
            }
        }

        // Se o campo tiver a anotação @Id, será a chave primária e pode ser AUTO_INCREMENT
        String columnDefinition = columnName + " " + columnType;
        return columnDefinition;
    }

    // Mapeia os tipos Java para tipos SQL
    private static String mapJavaTypeToSQL(Class<?> type) {
        if (type == int.class || type == Integer.class) return "INT";
        if (type == String.class) return "VARCHAR(255)";
        if (type == boolean.class || type == Boolean.class) return "BOOLEAN";
        if (type == double.class || type == Double.class) return "DOUBLE";
        if (type == long.class || type == Long.class) return "BIGINT";
        if (type == float.class || type == Float.class) return "FLOAT";
        if (type == java.util.Date.class) return "DATETIME";
        if (type == java.sql.Date.class) return "DATE";
        if (type == java.sql.Timestamp.class) return "TIMESTAMP";
        // Adicione mais tipos conforme necessário
        return "TEXT";
    }

    // Executa o comando SQL para criar a tabela no banco
    private static void executeSQL(String sql) {
        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            System.out.println("Executando SQL: " + sql);  // Verifique o SQL executado
            stmt.executeUpdate();
            System.out.println("Tabela criada com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao executar SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

}