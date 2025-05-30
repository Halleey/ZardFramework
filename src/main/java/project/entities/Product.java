package project.entities;

import configurations.dbas.*;

import java.math.BigDecimal;

@Entity
public class Product {

    @Id
    private Long id;
    @Column
    private String nome;
    @Column
    private BigDecimal price;

    @Column(blobType = BlobType.LARGE)
    byte[] largeimage;



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

    public byte[] getLargeimage() {
        return largeimage;
    }

    public void setLargeimage(byte[] largeimage) {
        this.largeimage = largeimage;
    }
}
