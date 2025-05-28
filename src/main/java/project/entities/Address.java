package project.entities;

import configurations.dbas.Column;
import configurations.dbas.Entity;
import configurations.dbas.Id;

import java.io.Serializable;

@Entity
public class Address implements Serializable {

    @Id
    private Long id;

    @Column
    private String street;

    @Column
    private String city;


    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
