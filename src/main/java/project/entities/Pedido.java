package project.entities;

import configurations.dbas.*;

import java.util.List;

@Entity
public class Pedido {
    @Id
    Long id;
    @Column
    private String name;
    @OneToMany(cascade = CascadeType.ALL)
    List<Product> products;
}
