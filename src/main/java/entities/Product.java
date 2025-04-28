package entities;

import dbas.Column;
import dbas.Entity;
import dbas.Id;

@Entity
public class Product {

    @Id
    Long id;
    @Column
    String nome;
    @Column
    double price;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
