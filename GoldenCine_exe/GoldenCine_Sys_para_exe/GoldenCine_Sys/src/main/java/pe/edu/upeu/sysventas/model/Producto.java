package pe.edu.upeu.sysventas.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.sysventas.enums.TipoFuncion;

@Data
@Builder
@NoArgsConstructor//te crea constructores //sin argumrntos
@AllArgsConstructor//todos los parametros de entrada
public class Producto {

    private Long idProducto;
    @NotBlank(message = "El nombre del Usuario es obligatorio")
    private String nombre;
    @NotNull(message = "EL tipo de funcion es obligatorio")
    private TipoFuncion tiipoProducto;

    @NotNull(message = "El precio de la pelicula es obligatorio")
    @Positive(message = "El precio de la pelicula debe ser positivo")
    private Double pu;
    @NotNull(message = "La pelicula de la funcion es obligatoria")
    private Peliculas idPelicula;
    @NotNull(message = "La sala de la funcion es obligatoria")
    private Sala idSala;
}