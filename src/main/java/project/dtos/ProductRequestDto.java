package project.dtos;
import java.math.BigDecimal;
public class ProductRequestDto {
    private String nome;
    private BigDecimal price;
    private byte[] image; // imagem opcional

    public String getNome() { return nome; }
    public BigDecimal getPrice() { return price; }
    public byte[] getImage() { return image; }

    public void setNome(String nome) { this.nome = nome; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setImage(byte[] image) { this.image = image; }
}
