package configurations.genericsRepositories;
import configurations.dbas.Entity;
import configurations.dbas.OneToOne;
import configurations.orm.ConnectionPool;
import configurations.dbas.Column;
import configurations.dbas.Id;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class GenericRepositoryImpl<T, ID> implements GenericRepository<T, ID> {

    private final Class<T> entityClass;
    private final String tableName;
    private final Field idField;
    private final List<Field> columnFields;
    private final List<Field> relationFields;

    public GenericRepositoryImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.tableName = entityClass.getSimpleName().toLowerCase();

        /*
         Percorre todos os campos da entidade, realiza o filtro e retorna somente o que for necessario em formato de lista
         */
        this.idField = Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Entidade " + entityClass.getSimpleName() + " não tem @Id"));

        // Campos simples (colunas + id)
        this.columnFields = Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Column.class) || f.isAnnotationPresent(Id.class))
                .collect(Collectors.toList());

        // Campos de relacionamento
        this.relationFields = Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(OneToOne.class))
                .collect(Collectors.toList());

        // Permitir acesso a campos privados
        this.idField.setAccessible(true);
        this.columnFields.forEach(f -> f.setAccessible(true));
        this.relationFields.forEach(f -> f.setAccessible(true));
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
                    Field relatedId = Arrays.stream(relatedEntity.getClass().getDeclaredFields())
                            .filter(f -> f.isAnnotationPresent(Id.class))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Entidade relacionada " +
                                    relatedEntity.getClass().getSimpleName() + " não tem @Id"));

                    relatedId.setAccessible(true);
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
                for (Field field : columnFields) {
                    Object value = rs.getObject(field.getName());
                    field.set(entity, value);
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