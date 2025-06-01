package project.dtos;
import java.math.BigDecimal;
public class ProductRequestDto {
    private String nome;
    private BigDecimal price;
    private byte[] image;

    public String getNome() { return nome; }
    public BigDecimal getPrice() { return price; }
    public byte[] getImage() { return image; }

    public void setImage(byte[] image) { this.image = image; }
}
