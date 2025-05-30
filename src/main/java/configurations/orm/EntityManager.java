package configurations.orm;


import configurations.dbas.*;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;


public class EntityManager {

    public static void generateSchema(String basePackage) {
        List<Class<?>> entities = getEntityClasses(basePackage);
        // Mapeia nome da tabela -> Classe Java (para uso posterior)
        Map<String, Class<?>> tableClassMap = new HashMap<>();

        // Grafo de dependências entre tabelas (usado para saber a ordem de criação)
        Map<String, TableDependency> dependencyGraph = new HashMap<>();

        // Constrói o grafo de dependência baseado nas anotações de relacionamentos
        for (Class<?> entityClass : entities) {
            validateEntityStructure(entityClass);
            Entity entity = entityClass.getAnnotation(Entity.class);
            String tableName = entity.value().isEmpty() ? entityClass.getSimpleName().toLowerCase() : entity.value();

            // Armazena o mapeamento da tabela com sua classe correspondente
            tableClassMap.put(tableName, entityClass);

            // Cria o objeto de dependência da tabela
            TableDependency td = new TableDependency(tableName);

            // Para cada campo da entidade, verifica se existe uma dependência (chave estrangeira)
            for (Field field : entityClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class)) {
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
        String tableName = entity.value().isEmpty()
                ? entityClass.getSimpleName().toLowerCase()
                : entity.value();

        List<String> columnDefs = new ArrayList<>();
        List<String> foreignKeys = new ArrayList<>();

        // Percorre todos os campos da classe para processar colunas e relacionamentos
        for (Field field : entityClass.getDeclaredFields()) {

            // Ignora campos anotados com @OneToMany, pois a FK está na outra ponta da relação
            if (field.isAnnotationPresent(OneToMany.class)) {
                continue;
            }

            // Se for @OneToOne ou @ManyToOne, gera uma chave estrangeira
            if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class)) {
                Class<?> refType = getAClass(field);

                String fkColumn = field.getName() + "_id";

                columnDefs.add(fkColumn + " BIGINT");

                String refTable = refType.getSimpleName().toLowerCase();

                // Verifica se a classe referenciada possui @OneToMany apontando para essa entidade, com cascade ativado
                boolean cascade = hasCascade(refType, entityClass);

                // Cria a cláusula da chave estrangeira
                String fk = "FOREIGN KEY (" + fkColumn + ") REFERENCES " + refTable + "(id)";
                if (cascade) {
                    // Adiciona a opção ON DELETE CASCADE se o relacionamento permitir
                    fk += " ON DELETE CASCADE";
                }

                // Adiciona a FK à lista
                foreignKeys.add(fk);
            } else {
                // Campo comum (sem relação), define a coluna normalmente
                columnDefs.add(buildColumnDefinition(field));
            }
        }

        // Adiciona as FKs no final da definição de colunas
        columnDefs.addAll(foreignKeys);

        // Monta a instrução SQL final
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (\n  " +
                String.join(",\n  ", columnDefs) + "\n);";

        // Exibe o SQL no console
        System.out.println("SQL gerado para a tabela '" + tableName + "':\n" + sql);

        // Executa o SQL no banco
        executeSQL(sql);
    }

    private static Class<?> getAClass(Field field) {
        Class<?> refType;

        // Precaução futura: se o campo for uma Collection (ex: List<Algo>), extrai o tipo genérico
        if (Collection.class.isAssignableFrom(field.getType())) {
            ParameterizedType listType = (ParameterizedType) field.getGenericType();
            refType = (Class<?>) listType.getActualTypeArguments()[0];
        } else {
            // Tipo direto (entidade referenciada)
            refType = field.getType();
        }
        return refType;
    }


    private static boolean hasCascade(Class<?> owningClass, Class<?> inverseClass) {
        for (Field field : owningClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(OneToMany.class)) {
                if (!Collection.class.isAssignableFrom(field.getType())) {
                    continue;
                }

                Type genericType = field.getGenericType();
                if (!(genericType instanceof ParameterizedType pt)) {
                    continue;
                }

                Type[] typeArgs = pt.getActualTypeArguments();
                if (typeArgs.length != 1 || !(typeArgs[0] instanceof Class<?> targetClass)) {
                    continue;
                }

                if (targetClass.equals(inverseClass)) {
                    OneToMany otm = field.getAnnotation(OneToMany.class);
                    return otm.cascade() == CascadeType.ALL;
                }
            }
        }
        return false;
    }

    private static String buildColumnDefinition(Field field) {
        String columnName = field.getName();
        boolean isRequired = false;

        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            if (!column.value().isEmpty()) {
                columnName = column.value();
            }
            isRequired = column.required();
        }

        StringBuilder columnDef = getStringBuilder(field, columnName, isRequired);

        return columnDef.toString();
    }
    private static StringBuilder getStringBuilder(Field field, String columnName, boolean isRequired) {
        String sqlType = mapJavaTypeToSQL(field);
        StringBuilder columnDef = new StringBuilder(columnName + " " + sqlType);

        if (field.isAnnotationPresent(Id.class)) {
            columnDef.append(" PRIMARY KEY AUTO_INCREMENT");
        } else {
            if (isRequired) {
                columnDef.append(" NOT NULL");
            }
        }

        if (field.isAnnotationPresent(Unique.class)) {
            Unique unique = field.getAnnotation(Unique.class);
            if (unique.value()) {
                columnDef.append(" UNIQUE");
            }
        }
        return columnDef;
    }


    private static String mapJavaTypeToSQL(Field field) {
        Class<?> type = field.getType();

        if (type == int.class || type == Integer.class) return "INT";
        if (type == String.class) return "VARCHAR(255)";
        if (type == BigDecimal.class) return "DECIMAL(15,2)";
        if (type == boolean.class || type == Boolean.class) return "BOOLEAN";
        if (type == double.class || type == Double.class) return "DOUBLE";
        if (type == long.class || type == Long.class) return "BIGINT";
        if (type == float.class || type == Float.class) return "FLOAT";
        if (type == java.util.Date.class) return "DATETIME";
        if (type == java.sql.Date.class) return "DATE";
        if (type == java.sql.Timestamp.class) return "TIMESTAMP";
        if (type == byte[].class) {
            Column column = field.getAnnotation(Column.class);
            BlobType blobType = column != null ? column.blobType() : BlobType.NONE;

            return switch (blobType) {
                case TINY -> "TINYBLOB";
                case NORMAL -> "BLOB";
                case MEDIUM -> "MEDIUMBLOB";
                case LARGE -> "LONGBLOB";
                default -> "BLOB";
            };
        }

        return "TEXT"; // Fallback genérico
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


    private static void validateEntityStructure(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {

            if (field.isAnnotationPresent(OneToMany.class)) {
                Type[] typeArgs = getTypes(entityClass, field);
                if (typeArgs.length != 1 || !(typeArgs[0] instanceof Class)) {
                    throw new IllegalStateException("Tipo genérico de '" + field.getName() + "' não pôde ser inferido corretamente.");
                }
            }

            if (field.isAnnotationPresent(OneToOne.class) || field.isAnnotationPresent(ManyToOne.class)) {
                if (field.getType().isPrimitive()) {
                    throw new IllegalStateException("Relacionamento em '" + field.getName() + "' não pode usar tipo primitivo.");
                }
            }
        }
    }

    private static Type[] getTypes(Class<?> entityClass, Field field) {
        if (!Collection.class.isAssignableFrom(field.getType())) {
            throw new IllegalStateException("Campo '" + field.getName() + "' da entidade '" + entityClass.getSimpleName()
                    + "' está anotado com @OneToMany mas não é uma Collection.");
        }

        // Verifica se o tipo genérico foi definido corretamente
        if (!(field.getGenericType() instanceof ParameterizedType pt)) {
            throw new IllegalStateException("Campo '" + field.getName() + "' da entidade '" + entityClass.getSimpleName()
                    + "' precisa definir o tipo genérico corretamente (ex: List<Algo>).");
        }

        return pt.getActualTypeArguments();
    }
}

