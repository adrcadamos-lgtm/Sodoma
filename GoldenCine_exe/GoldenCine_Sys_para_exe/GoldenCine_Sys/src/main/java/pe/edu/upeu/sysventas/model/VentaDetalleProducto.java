package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Tabla "venta_detalle_product" del diagrama: una línea de comida dentro de la venta.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaDetalleProducto {
    private Long idVentaDetProd;
    private Comida idProducto;
    private Integer cantidad;
    private Double pu;
    private Double subtotal;
}
