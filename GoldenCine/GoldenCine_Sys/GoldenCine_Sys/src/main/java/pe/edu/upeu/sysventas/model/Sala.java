package pe.edu.upeu.sysventas.model;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// CLASE Sala: es el molde. Cada "new Sala(...)" es un OBJETO (instancia) con su propio id y nombre.
// ENCAPSULAMIENTO: los atributos son private y se usan solo por getters/setters (los genera @Data de Lombok).
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sala {
    private Long idSala;

    @NotBlank(message = "El nombre de la sala es obligatorio")
    private String nombre;

    // Estado de la sala: true = Libre, false = Ocupada.
    private Boolean libre;

    // Texto del estado para mostrarlo en la tabla.
    public String getEstado() {
        return Boolean.TRUE.equals(libre) ? "Libre" : "Ocupada";
    }
}