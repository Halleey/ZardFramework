package project.entities;

import configurations.dbas.Column;
import configurations.dbas.Entity;
import configurations.dbas.Id;
import configurations.dbas.ManyToOne;

@Entity
public class Livros {

    @Id
    private Long id;

    @Column
    private String titulo;

    @ManyToOne
    private Autores autores;

    public Autores getAutores() {
        return autores;
    }

    public void setAutores(Autores autores) {
        this.autores = autores;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}

