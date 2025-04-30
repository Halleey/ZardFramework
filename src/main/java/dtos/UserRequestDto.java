package dtos;

public class UserRequestDto {
    String name;
    String email;
    String cpf;
    private Long addressId;

    public Long getAddressId() {
        return addressId;
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
