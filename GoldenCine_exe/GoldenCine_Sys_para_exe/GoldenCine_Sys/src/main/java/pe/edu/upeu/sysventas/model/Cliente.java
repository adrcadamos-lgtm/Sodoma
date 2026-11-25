package pe.edu.upeu.sysventas.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import pe.edu.upeu.sysventas.enums.TipoDocumento;
@Data
public class Cliente {
    @NotBlank(message = "El documento del cliente es obligatorio")
    String dniruc;
    @NotBlank(message = "El nombre del cliente es obligatorio")
    String nombres;
    String repLegal;
    @NotNull(message = "El tipo de documento es obligatorio")
    TipoDocumento tipoDocumento;
    @NotNull(message = "La edad es obligatoria")
    @Min(value = 0, message = "La edad no puede ser negativa")
    @Max(value = 120, message = "La edad no es válida")
    Integer edad;
}
