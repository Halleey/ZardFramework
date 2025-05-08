package configurations.genericsRepositories;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
// Classe responsável por fornecer instâncias de repositórios (DAOs) de forma centralizada
public class DaoFactory {

    // Cache que armazena instâncias já criadas de repositórios, evitando recriação
    // Usa ConcurrentHashMap para ser seguro em ambientes com múltiplas threads
    private static final Map<Class<?>, GenericRepository<?, ?>> repositoryCache = new ConcurrentHashMap<>();

    /**
     * Retorna uma instância de GenericRepository para a entidade passada.
     * Se ela ainda não estiver no cache, será criada uma nova e armazenada.
     */
    @SuppressWarnings("unchecked") // Suprime o aviso de cast genérico inseguro
    public static <T, ID> GenericRepository<T, ID> getDao(Class<T> clazz) {
        // Busca no cache ou cria uma nova instância usando createRepository()
        return (GenericRepository<T, ID>) repositoryCache.computeIfAbsent(clazz, DaoFactory::createRepository);
    }

    //Cria uma nova instância do repositório genérico para a entidade fornecida
    private static <T, ID> GenericRepository<T, ID> createRepository(Class<T> clazz) {
        // Cria uma nova instância do repositório genérico para a classe fornecida
        return new GenericRepositoryImpl<>(clazz);
    }
}