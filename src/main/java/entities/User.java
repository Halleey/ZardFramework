package entities;

import dbas.Column;
import dbas.Entity;
import dbas.Id;

@Entity
public class User {

    @Id
    long id;
    @Column
    String name;
}
