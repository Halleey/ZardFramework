package entities;

import configurations.dbas.Column;
import configurations.dbas.Entity;
import configurations.dbas.Id;
import configurations.dbas.OneToMany;

import java.util.List;

@Entity
public class Autores {

    @Id
    private Long id;
    @Column
    private String name;

    public String getName() {
        return name;
    }
    @OneToMany
    List<Livros> livros;

}
