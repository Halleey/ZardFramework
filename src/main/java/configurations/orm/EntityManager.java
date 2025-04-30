package configurations.orm;


import configurations.dbas.Column;
import configurations.dbas.Entity;
import configurations.dbas.Id;
import configurations.dbas.OneToOne;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


public class EntityManager {

    public static void generateSchema(String basePackage) {
        // Busca todas as classes anotadas com @Entity no pacote informado
        List<Class<?>> entities = getEntityClasses(basePackage);

        // Mapeia nome da tabela -> Classe Java (para uso posterior)
        Map<String, Class<?>> tableClassMap = new HashMap<>();

        // Grafo de dependências entre tabelas (usado para saber a ordem de criação)
        Map<String, TableDependency> dependencyGraph = new HashMap<>();

        // Constrói o grafo de dependência baseado nas anotações @OneToOne
        for (Class<?> entityClass : entities) {
            Entity entity = entityClass.getAnnotation(Entity.class);
            // Pega o nome da tabela (ou nome da classe em minúsculo)
            String tableName = entity.value().isEmpty() ? entityClass.getSimpleName().toLowerCase() : entity.value();

            // Armazena o mapeamento da tabela com sua classe correspondente
            tableClassMap.put(tableName, entityClass);

            // Cria o objeto de dependência da tabela
            TableDependency td = new TableDependency(tableName);

            // Para cada campo da entidade, verifica se existe uma dependência (chave estrangeira)
            for (Field field : entityClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(OneToOne.class)) {
                    // Adiciona a tabela referenciada como dependência
                    String referencedTable = field.getType().getSimpleName().toLowerCase();
                    td.addDependency(referencedTable);
                }
            }

            // Adiciona a tabela e suas dependências no grafo
            dependencyGraph.put(tableName, td);
        }

        // Ordena as tabelas de forma que as dependências venham primeiro
        List<String> orderedTables = DependencyResolver.resolveOrder(dependencyGraph);

        // Cria as tabelas na ordem correta (respeitando as chaves estrangeiras)
        for (String tableName : orderedTables) {
            Class<?> clazz = tableClassMap.get(tableName);
            if (clazz != null) {
                generateTable(clazz);
            }
        }
    }

    private static List<Class<?>> getEntityClasses(String basePackage) {
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> entityClasses = reflections.getTypesAnnotatedWith(Entity.class);
        System.out.println("Tabelas encontradas com @Entity: " + entityClasses.size());
        return new ArrayList<>(entityClasses);
    }

    private static void generateTable(Class<?> entityClass) {
        Entity entity = entityClass.getAnnotation(Entity.class);
        String tableName = entity.value().isEmpty() ? entityClass.getSimpleName().toLowerCase() : entity.value();

        List<String> columnDefs = new ArrayList<>();
        List<String> foreignKeys = new ArrayList<>();

        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(OneToOne.class)) {
                String fkColumn = field.getName() + "_id";
                columnDefs.add(fkColumn + " BIGINT");

                String refTable = field.getType().getSimpleName().toLowerCase();
                foreignKeys.add("FOREIGN KEY (" + fkColumn + ") REFERENCES " + refTable + "(id)");
            } else {
                columnDefs.add(buildColumnDefinition(field));
            }
        }

        columnDefs.addAll(foreignKeys);

        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (\n  " +
                String.join(",\n  ", columnDefs) + "\n);";

        System.out.println("SQL gerado para a tabela '" + tableName + "':\n" + sql);
        executeSQL(sql);
    }

    private static String buildColumnDefinition(Field field) {
        String columnName = field.getName();
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            if (!column.value().isEmpty()) {
                columnName = column.value();
            }
        }

        String sqlType = mapJavaTypeToSQL(field.getType());
        StringBuilder columnDef = new StringBuilder(columnName + " " + sqlType);

        if (field.isAnnotationPresent(Id.class)) {
            columnDef.append(" PRIMARY KEY AUTO_INCREMENT");
        }

        return columnDef.toString();
    }

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
        return "TEXT";
    }

    private static void executeSQL(String sql) {
        try (Connection conn = ConnectionPool.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela criada com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao executar SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

