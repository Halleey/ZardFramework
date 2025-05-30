package project.dtos;

import java.math.BigDecimal;

public class ProductRequestDto {
    String nome;
    BigDecimal price;

    public String getNome() {
        return nome;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
