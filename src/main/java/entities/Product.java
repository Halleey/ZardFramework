package entities;

import configurations.dbas.Column;
import configurations.dbas.Entity;
import configurations.dbas.Id;
import configurations.dbas.ManyToOne;

@Entity
public class Product {

    @Id
    private Long id;
    @Column
    private String nome;
    @Column
    private double price;
    @ManyToOne
    private Pedido ordersPedido;
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
