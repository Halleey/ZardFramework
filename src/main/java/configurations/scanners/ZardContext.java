package configurations.scanners;

import configurations.genericsRepositories.RepositoryFactory;
import configurations.instancias.Repository;
import configurations.instancias.RestController;
import configurations.instancias.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.*;
public class ZardContext {

    // Container principal de instâncias gerenciadas pelo framework (como um mini Spring)
    private final Map<Class<?>, Object> container = new HashMap<>();

    // Metodo principal que inicializa todo o contexto, escaneando o pacote e instanciando os componentes
    public void initialize(String basePackage) throws Exception {

        // Instancia todos os repositórios anotados com @Repository
        for (Class<?> clazz : ClassScanner.getAnnotatedClasses(basePackage, Repository.class)) {
            // Recupera a interface genérica implementada pelo repositório (ex: UserRepository extends GenericRepository<Users, Long>)
            ParameterizedType genericInterface = (ParameterizedType) clazz.getGenericInterfaces()[0];

            // Extrai o tipo da entidade (Users, Product, etc.)
            Class<?> entityClass = (Class<?>) genericInterface.getActualTypeArguments()[0];

            // Cria o repositório dinamicamente via fábrica
            Object instance = RepositoryFactory.createRepository(clazz, entityClass);

            // Adiciona o repositório instanciado ao contêiner
            container.put(clazz, instance);
        }

        // Instancia todos os serviços anotados com @Service
        for (Class<?> clazz : ClassScanner.getAnnotatedClasses(basePackage, Service.class)) {
            // Cria instância do serviço e injeta as dependências necessárias
            instantiateWithDependencies(clazz);
        }

        // 🔹 3. Instancia todos os controladores anotados com @RestController
        for (Class<?> clazz : ClassScanner.getAnnotatedClasses(basePackage, RestController.class)) {
            // Cria instância do controller e injeta as dependências necessárias
            instantiateWithDependencies(clazz);
        }
    }

    // Método auxiliar para criar instâncias de classes com dependências injetadas via construtor
    private void instantiateWithDependencies(Class<?> clazz) throws Exception {
        // Pega o primeiro construtor público da classe
        Constructor<?> constructor = clazz.getConstructors()[0];

        // Para cada parâmetro do construtor, busca a instância correspondente no contêiner
        Object[] params = Arrays.stream(constructor.getParameterTypes())
                .map(container::get) // injeta dependência já criada
                .toArray();

        // Cria a instância da classe passando as dependências
        Object instance = constructor.newInstance(params);

        // Armazena a nova instância no contêiner
        container.put(clazz, instance);
    }

    // Recupera uma instância já gerenciada no contêiner
    public <T> T get(Class<T> clazz) {
        return clazz.cast(container.get(clazz));
    }

    // Retorna uma lista de todas as instâncias de controllers registrados
    public Collection<Object> getControllers() {
        return container.entrySet().stream()
                .filter(e -> e.getKey().isAnnotationPresent(RestController.class))
                .map(Map.Entry::getValue)
                .toList();
    }
}
