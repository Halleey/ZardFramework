package entities;

import configurations.dbas.Entity;
import configurations.dbas.Id;

@Entity
public class macaco {

    @Id
    Long id;
    String nome;
    String cabelo;
}
