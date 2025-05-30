# ZardFramework - Microframework Web Java

> *Um mini framework web criado para aprendizado e experimentação. Sem fins lucrativos. Livre para uso, modificação (desde que sejam apresentadas as modificações feitas) e aprendizado.*

---

## 🌟 Visão Geral

O **ZardFramework** é um microframework construído em Java, focado em simplicidade, aprendizado e extensibilidade. Inclui recursos robustos como:

* 🔗 **Roteamento baseado em anotações** (`@GetRouter`, `@PostRouter`, etc.)
* 🏛️ **ORM própria com suporte a:**

  * Geração automática de tabelas
  * Chaves estrangeiras
  * Relacionamentos `1:1`, `1:N`, `N:1`
  * Queries personalizadas via anotação `@Querys`
* ⚖️ **Padrão MVC:** Controllers, Services e Repositories
* 🌐 **Suporte a `@PathParam` e `@QueryParam`**
* ✅ **Verbos HTTP completos:** `GET`, `POST`, `PATCH`, `DELETE` (futuro suporte a `PUT`)
* ✨ **Serialização automática para JSON** via refleção
* 🔒 **Autenticação JWT com controle de acesso baseado em roles**
* ⚖️ **Configuração flexível de rotas protegidas via `@EnableSecurity`**
* 🚀 **Resposta padronizada com `ResponseEntity`**

---

## 🖊️ Anotações Disponíveis

### Roteamento

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GetRouter { String value(); }

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostRouter { String value(); }
// PATCH, DELETE também estão disponíveis
```

### ORM

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface Entity { String value() default ""; }

@Retention(RetentionPolicy.RUNTIME)
public @interface Id {}

@Retention(RetentionPolicy.RUNTIME)
public @interface Column { String value() default ""; }

@Retention(RetentionPolicy.RUNTIME)
public @interface Querys { String value(); }
```

### Request Mapping

```java
@Target(ElementType.PARAMETER)
public @interface PathParam { String value(); }

@Target(ElementType.PARAMETER)
public @interface QueryParam { String value(); }
```

---

## 📂 Exemplo CRUD com Usuários

### 1. Entidade

```java
@Entity
public class Users {
    @Id
    private Long id;

    @Column("name")
    private String name;

    @Column("email")
    private String email;

    @Column("cpf")
    private String cpf;
}
```

### 2. Repository

```java
public interface UserRepository extends GenericRepository<Users, Long> {
    @Querys("SELECT * FROM users WHERE name = ?")
    List<Users> findByName(String name);
}
```

### 3. Service

```java
public class UserService {
    public void createUser(UserRequestDto requestDto);
    public List<Users> getAll();
    public boolean deleteUser(Long id);
}
```

### 4. Controller

```java
@RestController
@RequestController("/user")
public class ControllerTeste {
    @GetRouter("/todos")
    public ResponseEntity<List<Users>> pegatodos();

    @PostRouter("/save")
    public ResponseEntity<String> saveUser(UserRequestDto dto);

    @DeleteRouter("/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathParam("id") Long id);
}
```

---

## ⚙️ Inicialização da Aplicação

```java
public class ZardFrameworkApplication {
  public static void main(String[] args) throws Exception {
    Server app = new Server(8080);

    EntityManager.generateSchema("project/entities");
    ZardContext context = new ZardContext();
    context.initialize("project");

    for (Object controller : context.getControllers()) {
      RouterRegister.registerRoutes(app, controller);
    }

    app.start();
  }
}
```

---

## 🚀 Exemplos de Requisição

### GET `/user/todos`

* Lista todos os usuários cadastrados.

### POST `/user/save`

**Body JSON:**

```json
{
  "name": "Zard",
  "email": "zard@email.com",
  "cpf": "123.456.789-00"
}
```

### DELETE `/user/delete/{id}`

* Remove o usuário com base no ID.

---

## 🔒 Exemplo de Configuração de Autenticação e Autorizacão

```java
@EnableSecurity
public class MySecurityConfig extends SecurityConfig {
    @Override
    public void configure() {
        permit("POST", "/user/check-password");
        permit("GET", "/publico/hello");
        permit("POST", "/user/login");
        permit("POST", "/user/save");

        hasRole("DELETE", "/user/delete/{id}", "admin");
        hasRole("GET", "/user", "admin");
        hasRole("GET", "/user/equals", "admin", "gestor");

        addFilter(new AuthFilter(), new RoleFilter(getRouteControl()));
    }
}
```

---

## 🔢 Roadmap Futuro

* ✈️ Criação de interceptadores de requisições
* ⚖️ Criação de relação N\:N

---

> Desenvolvido com paixão e curiosidade por **Zard**. Aprender construindo é a melhor forma de dominar qualquer tecnologia.

