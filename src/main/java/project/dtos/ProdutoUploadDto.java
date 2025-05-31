package project.dtos;


import java.math.BigDecimal;
public class ProdutoUploadDto {
    private String nome;
    private BigDecimal price;


    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

}
