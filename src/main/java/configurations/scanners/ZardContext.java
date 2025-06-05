package configurations.scanners;

import configurations.genericsRepositories.RepositoryFactory;
import configurations.genericsRepositories.annotations.Repository;
import configurations.core.routes.annotations.RestController;
import configurations.genericsRepositories.annotations.Service;
import configurations.security.configcors.CorsConfiguration;
import configurations.security.EnableCors;
import configurations.security.EnableSecurity;
import configurations.security.auth.SecurityConfig;
import configurations.security.configcors.CorsHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.*;
public class ZardContext {

    // Container principal de instâncias gerenciadas pelo framework (como um mini Spring)
    private final Map<Class<?>, Object> container = new HashMap<>();

    // Metodo principal que inicializa todo o contexto, escaneando o pacote e instanciando os componentes
    public void initialize(String basePackage) throws Exception {



        // Instancia a classe de configuração de CORS anotada com @EnableCors
        Set<Class<?>> corsConfigs = ClassScanner.getAnnotatedClasses(basePackage, EnableCors.class);
        if (!corsConfigs.isEmpty()) {
            if (corsConfigs.size() > 1) {
                throw new RuntimeException("Apenas uma classe pode ser anotada com @EnableCors.");
            }

            Class<?> configClass = corsConfigs.iterator().next();

            CorsConfiguration corsConfiguration = (CorsConfiguration) configClass.getDeclaredConstructor().newInstance();

            container.put(CorsConfiguration.class, corsConfiguration);

            // Cria e registra o CorsHandler, que é o interceptor que vai usar essa config
            CorsHandler corsHandler = new CorsHandler(corsConfiguration);
            container.put(CorsHandler.class, corsHandler);
        }


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

        // Instancia a classe de configuração de segurança anotada com @EnableSecurity
        Set<Class<?>> securityConfigs = ClassScanner.getAnnotatedClasses(basePackage, EnableSecurity.class);
        if (!securityConfigs.isEmpty()) {
            if (securityConfigs.size() > 1) {
                throw new RuntimeException("Apenas uma classe pode ser anotada com @EnableSecurity.");
            }

            Class<?> configClass = securityConfigs.iterator().next();

            // Cria a instância da configuração
            SecurityConfig securityConfig = (SecurityConfig) configClass.getDeclaredConstructor().newInstance();

            // Executa o método configure()
            securityConfig.configure();

            // Armazena como singleton global do tipo base
            container.put(SecurityConfig.class, securityConfig);
        }


        // Instancia todos os serviços anotados com @Service
        for (Class<?> clazz : ClassScanner.getAnnotatedClasses(basePackage, Service.class)) {
            // Cria instância do serviço e injeta as dependências necessárias
            instantiateWithDependencies(clazz);
        }

        // Instancia todos os controladores anotados com @RestController
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


    /**
     * Retorna a instância do tipo, ou null se não estiver registrada.
     */
    public <T> T getOptional(Class<T> clazz) {
        Object instance = container.get(clazz);
        if (instance == null) {
            return null;
        }
        return clazz.cast(instance);
    }

    /**
     * Retorna todas as instâncias que são do tipo informado (ou subclasses/implementações).
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getBeansOfType(Class<T> type) {
        return container.values().stream()
                .filter(type::isInstance)
                .map(obj -> (T) obj)
                .toList();
    }

}
