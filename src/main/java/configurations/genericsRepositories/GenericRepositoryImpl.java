package configurations.genericsRepositories;
import configurations.dbas.*;
import configurations.orm.ConnectionPool;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GenericRepositoryImpl<T, ID> implements GenericRepository<T, ID> {

    private final Class<T> entityClass;
    private final String tableName;
    private final Field idField;
    private final List<Field> columnFields;
    private final List<Field> relationFields;
    private static final Map<Class<?>, Field> ID_FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Field>> COLUMN_FIELDS_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, List<Field>> RELATION_FIELDS_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field> RELATED_ID_CACHE = new ConcurrentHashMap<>();

    public GenericRepositoryImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.tableName = entityClass.getSimpleName().toLowerCase();

        /*
         Percorre todos os campos da entidade, realiza o filtro e retorna somente o que for necessario em formato de lista
         */
        this.idField = ID_FIELD_CACHE.computeIfAbsent(entityClass, cls ->
                Arrays.stream(cls.getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(Id.class))
                        .peek(f -> f.setAccessible(true))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Entidade " + cls.getSimpleName() + " não tem @Id"))
        );

        this.columnFields = COLUMN_FIELDS_CACHE.computeIfAbsent(entityClass, cls ->
                Arrays.stream(cls.getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(Column.class) || f.isAnnotationPresent(Id.class))
                        .peek(f -> f.setAccessible(true))
                        .collect(Collectors.toList())
        );

        this.relationFields = RELATION_FIELDS_CACHE.computeIfAbsent(entityClass, cls ->
                Arrays.stream(cls.getDeclaredFields())
                        .filter(f -> f.isAnnotationPresent(OneToOne.class) || f.isAnnotationPresent(OneToMany.class) || f.isAnnotationPresent(ManyToOne.class))
                        .peek(f -> f.setAccessible(true))
                        .collect(Collectors.toList())
        );

//        // Permitir acesso a campos privados
//        this.idField.setAccessible(true);
//        this.columnFields.forEach(f -> f.setAccessible(true));
//        this.relationFields.forEach(f -> f.setAccessible(true));
    }

    @Override
    public void save(T entity) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        List<String> columns = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        // Campos simples (nome, email, etc)
        for (Field field : columnFields) {
            columns.add(field.getName());
            placeholders.add("?");
            try {
                values.add(field.get(entity));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Erro ao acessar campo: " + field.getName(), e);
            }
        }
        // Campos de relacionamento (fk)
        for (Field relationField : relationFields) {
            String columnName = relationField.getName() + "_id"; // ex: address -> address_id
            columns.add(columnName);
            placeholders.add("?");

            try {
                Object relatedEntity = relationField.get(entity);
                if (relatedEntity != null) {
                    Field relatedId = RELATED_ID_CACHE.computeIfAbsent(relatedEntity.getClass(), cls ->
                            Arrays.stream(cls.getDeclaredFields())
                                    .filter(f -> f.isAnnotationPresent(Id.class))
                                    .peek(f -> f.setAccessible(true))
                                    .findFirst()
                                    .orElseThrow(() -> new RuntimeException("Entidade relacionada " +
                                            cls.getSimpleName() + " não tem @Id"))
                    );

                    Object fkValue = relatedId.get(relatedEntity);
                    values.add(fkValue);
                } else {
                    values.add(null);
                }
            } catch (Exception e) {
                throw new RuntimeException("Erro ao processar relacionamento: " + relationField.getName(), e);
            }
        }

        sql.append(String.join(", ", columns));
        sql.append(") VALUES (");
        sql.append(String.join(", ", placeholders));
        sql.append(");");

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<T> findAll() {
        List<T> results = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                T entity = entityClass.getDeclaredConstructor().newInstance();

                // Campos simples (colunas + id)
                for (Field field : columnFields) {
                    Object value = rs.getObject(field.getName());
                    field.set(entity, value);
                }

                // Campos de relacionamento (fk)
                for (Field fieldRelations : relationFields) {
                    String fkColumn = fieldRelations.getName() + "_id";
                    Object fkValue = rs.getObject(fkColumn);

                    if (fkValue != null) {
                        Class<?> relatedClass = fieldRelations.getType();

                        // Obtém o DAO da entidade relacionada
                        GenericRepository<?, Object> relatedDao = DaoFactory.getDao(relatedClass);

                        // Busca a entidade relacionada pelo ID da FK
                        Object relatedEntity = relatedDao.findById(fkValue)
                                .orElse(null);

                        fieldRelations.set(entity, relatedEntity);
                    }
                }

                results.add(entity);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return results;
    }

    @Override
    public List<T> findAllOnlyReferences() {
        List<T> results = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                T entity = entityClass.getDeclaredConstructor().newInstance();

                // Preenche campos simples (colunas + id)
                for (Field field : columnFields) {
                    Object value = rs.getObject(field.getName());
                    field.set(entity, value);
                }

                // Preenche somente o ID das relações, não a entidade completa
                for (Field relationField : relationFields) {
                    String fkColumn = relationField.getName() + "_id";
                    Object fkValue = rs.getObject(fkColumn);

                    if (fkValue != null) {
                        // Cria uma instância "vazia" da classe relacionada, com apenas o ID setado
                        Object relatedInstance = relationField.getType().getDeclaredConstructor().newInstance();

                        // Procura o campo que é @Id na classe relacionada
                        Field idField = Arrays.stream(relationField.getType().getDeclaredFields())
                                .filter(f -> f.isAnnotationPresent(Id.class)) // ou use outra forma de identificar o ID
                                .findFirst()
                                .orElseThrow(() -> new RuntimeException("ID field not found in " + relationField.getType()));

                        idField.setAccessible(true);
                        idField.set(relatedInstance, fkValue);

                        // Define no campo da entidade principal
                        relationField.setAccessible(true);
                        relationField.set(entity, relatedInstance);
                    }
                }

                results.add(entity);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return results;
    }

    @Override
    public Optional<T> findById(ID id) {
        String sql = "SELECT * FROM " + tableName + " WHERE " + idField.getName() + " = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    T entity = entityClass.getDeclaredConstructor().newInstance();
                    for (Field field : columnFields) {
                        Object value = rs.getObject(field.getName());
                        field.set(entity, value);
                    }
                    return Optional.of(entity);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public void deleteById(ID id) {
        String sql = "DELETE FROM " + tableName + " WHERE " + idField.getName() + " = ?";

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(T entity) {
        StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET ");
        List<String> setClauses = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Field field : columnFields) {
            if (!field.equals(idField)) { // Não atualiza o ID
                setClauses.add(field.getName() + " = ?");
                try {
                    values.add(field.get(entity));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        sql.append(String.join(", ", setClauses));
        sql.append(" WHERE ").append(idField.getName()).append(" = ?");

        try (Connection conn = ConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }

            // Seta o ID como último parâmetro
            stmt.setObject(values.size() + 1, idField.get(entity));

            stmt.executeUpdate();
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    public Class<T> getEntityClass() {
        return entityClass;
    }
}
