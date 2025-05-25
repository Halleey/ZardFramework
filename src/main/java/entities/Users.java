package entities;

import configurations.dbas.Column;
import configurations.dbas.Entity;
import configurations.dbas.Id;
import configurations.dbas.OneToOne;

@Entity()
public class Users {
    @Id
    private  long id;
    @Column
    private String name;
    @Column
    private String password;
    @Column
    private String email;
    @Column
    private String cpf;
    @OneToOne(referencedColumnName = "address_id")
    private Address address;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

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
