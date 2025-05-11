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

## Algumas das Anotações Utilizadas.

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

### 3. Criação da interface para injetar o repositorio

```java
public interface ProductRepository extends GenericRepository<Product, Long> {

    @Querys("SELECT * FROM product WHERE nome = ?")
    List<Product> findByName(String name);
}

```


### 4. Service: UserService

```java
public class UserService {

  private final UserRepository repository;

  public UserService(UserRepository repository) {
    this.repository = repository;
  }

  public void createUser(UserRequestDto  requestDto) {
    Users user = new Users();
    user.setName(requestDto.getName());
    user.setEmail(requestDto.getEmail());
    user.setCpf(requestDto.getCpf());
    repository.save(user);
  }

  public List<Users> getAll() {
    return repository.findAll();

  }

  public boolean deleteUser(Long id) {
    Optional<Users> user = repository.findById(id);
    if (user.isPresent()) {
      repository.deleteById(id);
      return true;
    } else {
      return false;
    }
  }
}
```

---

### 5. Controller: ControllerTeste

```java


@RestController
@RequestController("/user")
public class ControllerTeste {

  private final UserService service;

  public ControllerTeste(UserService service) {
    this.service = service;
  }

  @GetRouter("")
  public String helloHandler()  {
    return "Lets go !";
  }
  @GetRouter("/hello")
  public ResponseEntity<String> hello() {
    return ResponseEntity.ok("Hello, luc");
  }

  //Versãoc Arcaica

  @GetRouter("/all")
  public String getAll() {
    // Chama o serviço para pegar todos os usuários
    List<Users> usersList = service.getAll();
    // Envia a resposta com a lista de usuários
    return JsonUtils.toJson(usersList);
  }

  //Versãoc com ResponseEntity

  @GetRouter("/todos")
  public ResponseEntity<List<Users>> pegatodos() {
    // Chama o serviço para pegar todos os usuários
    List<Users> usersList = service.getAll();
    // Envia a resposta com a lista de usuários
    return ResponseEntity.ok(usersList);
  }

  @DeleteRouter("/delete")
  public void deleteUser(Request req, Response res) throws IOException {
    String idStr = req.extractPathParam("/user/delete");
    Long id = Long.valueOf(idStr);

    boolean deleted = service.deleteUser(id);

    if (deleted) {
      res.send("Usuário deletado com sucesso!");
    } else {
      res.send("Usuário não encontrado!");
    }
  }

  @GetRouter("/find")
  public ResponseEntity<List<Users>> findIdParam(@QueryParam("id") Long id) {
    List<Users> users = service.getUserById(id);
    return ResponseEntity.ok(users);
  }

  //Nova versão
  //usamos o curinga no response para podermos trabalhar tanto com serialização json
  // quanto retorno em String personalizado
  @GetRouter("/equals")
  public ResponseEntity<?> getEqualsName(@QueryParam("name") String name) {
    if (name == null || name.isBlank()) {
      return ResponseEntity.status(400, "erro, parametro obrigatorio faltando");
    }

    List<UserResponseDTO> users = service.getUsersByName(name);
    return ResponseEntity.ok(users);
  }


  //SAVE USER NEW MODEL
  //ele recebe a DTO direto via reflexão
  @PostRouter("/save")
  public ResponseEntity<String> saveUser(UserRequestDto requestDto) {
    service.createUser(requestDto); // já recebeu o DTO pronto
    return ResponseEntity.status(201, "Salvando no novo modelo");
  }


  //SAVE USER OLD MODEL
  @PostRouter("/salvar")
  public void salvarVelho(Request req, Response response) throws IOException {
    String body = req.getBody(); // JSON vindo no body

    UserRequestDto user = JsonUtils.fromJson(body, UserRequestDto.class); // transforma JSON -> Users

    service.createUser(user); // chama o service certinho
    response.send("Salvando da forma velha");
  }
}

```

---

### 6. Inicialização: ZardFrameworkApplication

```java

    public class ZardFrameworkApplication {
	    public static void main(String[] args) throws Exception {
		Server app = new Server(8080);
		//coloque aqui, o pacote principal onde se encontra suas entidades
		EntityManager.generateSchema("entities");
		ZardContext context = new ZardContext();
		//coloque aqui, o pacote principal do seu projeto
		context.initialize("project");

		for (Object controller : context.getControllers()) {
			RouterRegister.registerRoutes(app, controller);
		}
		// De resto, não tem ncessidade alterar.
		app.start();
	}
}
```

---

## Testando as Rotas

- `GET /user`
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



