package pe.edu.upeu.sysventas.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.sysventas.enums.TipoComida;

// Tabla "producto" del diagrama: la comida/bebida que se vende junto con la entrada.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comida {
    private Long idComida;//encapsulamiento

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    @NotNull(message = "El tipo de producto es obligatorio")
    private TipoComida tipo;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private Double precio;

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stockDisponible;
}
