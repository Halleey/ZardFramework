# ZardFramework - Exemplo de CRUD básico

*Um mini framework para web, para aprender mais a fundo como os demais operam, sem fins lucrativos, sinta-se livre para utilizar e aprender junto*

Este é um exemplo básico de um CRUD usando o **ZardFramework**, com base no que montamos até agora.

Inclui:
- Um `GenericRepository` genérico.
- Uma implementação concreta `GenericRepositoryImpl`.
- Um `UserService` para a lógica de negócio.
- Um `ControllerTeste` com rotas GET e POST.
- Um `ZardFrameworkApplication` para iniciar tudo.
- Além de notations :Onde apenas com @ podemos definir funcionalidades.
---

## Anotações Utilizadas

### **@Target(ElementType.METHOD)**
A anotação `@Target` define o tipo de elemento onde a anotação pode ser aplicada. No caso de `@GetRouter` e `@PostRouter`, elas podem ser aplicadas **apenas em métodos**.

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetRouter {
    String value();
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostRouter {
    String value();
}


```

*RetentionPolicy.Runtime*: A anotação `@Retention` determina por quanto tempo a anotação deve ser mantida. Usamos RUNTIME para que a anotação esteja disponível em tempo de execução.


## Entity

*Define* que uma classe que possua a anotação `@Entity` será uma classe modelo para criação de uma tabela no banco de dados
````
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity {
    String value() default "";
}

````
##  @Id
*Marca o campo que representa o ID da entidade*. Este campo será usado para identificação única no banco de dados.

````
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
}

````
##  @Column
*Define um campo como uma coluna mapeada para o banco de dados*.

````
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String value() default "";
}
````

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



