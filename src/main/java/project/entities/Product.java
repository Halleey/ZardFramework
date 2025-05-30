package project.entities;

import configurations.dbas.Column;
import configurations.dbas.Entity;
import configurations.dbas.Id;
import configurations.dbas.ManyToOne;

import java.math.BigDecimal;

@Entity
public class Product {

    @Id
    private Long id;
    @Column
    private String nome;
    @Column
    private BigDecimal price;
    @ManyToOne
    private Pedido ordersPedido;
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
