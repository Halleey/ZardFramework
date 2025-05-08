package configurations.genericsRepositories;

import java.util.List;
import java.util.Optional;

public interface GenericRepository<T, ID> {
    void save(T entity);
    List<T> findAll();

    List<T> findAllOnlyReferences();

    Optional<T> findById(ID id);
    void deleteById(ID id);
    void update(T entity);
}
