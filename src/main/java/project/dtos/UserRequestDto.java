package project.dtos;

public class UserRequestDto {
    private String name;
    private String password;
    private String email;
    private String cpf;
    private String role;

    public String getRole() {
        return role;
    }

    private Long address_id;

    public Long getAddress_id() {
        return address_id;
    }

    public void setAddress_id(Long address_id) {
        this.address_id = address_id;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }
}
