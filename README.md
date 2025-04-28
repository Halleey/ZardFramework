# ZardFramework - Exemplo de CRUD básico

Este é um exemplo básico de um CRUD usando o **ZardFramework**, com base no que montamos até agora.

Inclui:
- Um `GenericRepository` genérico.
- Uma implementação concreta `GenericRepositoryImpl`.
- Um `UserService` para a lógica de negócio.
- Um `ControllerTeste` com rotas GET e POST.
- Um `ZardFrameworkApplication` para iniciar tudo.

---

## Estrutura de Código

### 1. Interface GenericRepository

```java
public interface GenericRepository<T, ID> {
    void save(T entity);
    List<T> findAll();
    Optional<T> findById(ID id);
    void deleteById(ID id);
}
```

### 2. Implementação GenericRepositoryImpl

```java
public class GenericRepositoryImpl<T, ID> implements GenericRepository<T, ID> {

    private final Class<T> entityClass;
    private final String tableName;
    private final Field idField;
    private final List<Field> columnFields;

    public GenericRepositoryImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.tableName = entityClass.getSimpleName().toLowerCase();

        this.idField = Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Entidade " + entityClass.getSimpleName() + " não tem @Id"));

        this.columnFields = Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Column.class) || f.isAnnotationPresent(Id.class))
                .collect(Collectors.toList());

        this.idField.setAccessible(true);
        this.columnFields.forEach(f -> f.setAccessible(true));
    }

    @Override
    public void save(T entity) {
        // Monta e executa o SQL de INSERT
    }

    @Override
    public List<T> findAll() {
        // Executa o SQL de SELECT *
    }

    @Override
    public Optional<T> findById(ID id) {
        // Executa o SQL de SELECT WHERE ID
    }

    @Override
    public void deleteById(ID id) {
        // Executa o SQL de DELETE WHERE ID
    }
}
```

_(Obs: no seu código fonte, cada método se encontra completo, de cada um desses está implementado corretamente com JDBC)_

---

### 3. Service: UserService

```java
public class UserService {
    private final GenericRepository<Users, Long> genericRepository;

    public UserService(GenericRepository<Users, Long> genericRepository) {
        this.genericRepository = genericRepository;
    }

    public void createUser(String name, String email, String cpf) {
        Users user = new Users();
        user.setId(System.currentTimeMillis()); // Gera ID automático
        user.setName(name);
        user.setEmail(email);
        user.setCpf(cpf);
        genericRepository.save(user);
    }
}
```

---

### 4. Controller: ControllerTeste

```java
public class ControllerTeste {

    private final UserService service;

    public ControllerTeste(UserService service) {
        this.service = service;
    }

    @GetRouter("/v2")
    public void helloHandler(Request req, Response res) throws IOException {
        res.send("Let's go!");
    }

    @PostRouter("/save")
    public void saveUser(Request req, Response res) throws IOException {
        String body = req.getBody();

        Users user = JsonUtils.fromJson(body, Users.class); // Converte JSON para objeto

        service.createUser(user.getName(), user.getEmail(), user.getCpf());

        res.send("Usuário salvo!");
    }
}
```

---

### 5. Inicialização: ZardFrameworkApplication

```java

public class ZardFrameworkApplication {
	public static void main(String[] args) throws IOException {
		Server app = new Server(8080);

		//Escanear o pacote para gerar as entidades.
		EntityManager.generateSchema("entities");


		// 1. Cria o repositório para Users
		GenericRepository<Users, Long> userRepository = new GenericRepositoryImpl<>(Users.class);

		// 2. Cria o UserService, passando o repositório
		UserService userService = new UserService(userRepository);

		// 3. Cria o ControllerTeste, passando o UserService
		ControllerTeste controllerTeste = new ControllerTeste(userService);
		RouterRegister.registerRoutes(app, controllerTeste);

		app.start();
	}
}
```

---

## Testando as Rotas

- `GET /v2`
    - Resposta: `"Let's go!"`

- `POST /save`
    - Body JSON Exemplo:
    ```json
    {
        "name": "Zard",
        "email": "zard@email.com",
        "cpf": "123.456.789-00"
    }
    ```
    - Resposta: `"Usuário salvo!"`

---

# Finalização

Pronto! Agora você tem um mini CRUD funcional que já salva dados no banco, usando as suas anotações personalizadas (@Id, @Column)



