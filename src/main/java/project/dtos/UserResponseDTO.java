package project.dtos;

public class UserResponseDTO {
    private String name;
    private Long address_id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getAddress_id() {
        return address_id;
    }

    public void setAddress_id(Long address_id) {
        this.address_id = address_id;
    }
}
