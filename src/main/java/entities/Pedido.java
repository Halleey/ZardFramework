package entities;

import configurations.dbas.*;

import java.util.List;

@Entity
public class Pedido {

    @Id
    Long id;
    @Column
    private String name;
    @OneToMany
    List<Product> products;
}
