package entities;

import configurations.dbas.Column;
import configurations.dbas.Entity;
import configurations.dbas.Id;

@Entity
public class Users {
    @Id
    long id;
    @Column
    String name;
    @Column
    String email;
    @Column
    String cpf;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}
