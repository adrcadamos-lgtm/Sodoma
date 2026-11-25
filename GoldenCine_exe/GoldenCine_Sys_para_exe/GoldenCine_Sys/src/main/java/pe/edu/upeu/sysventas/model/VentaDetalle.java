package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaDetalle {
    private Long idVentaDetalle;
    private Double pu;
    private Double cantidad;
    private Double descuento;
    private Double subtotal;
    private Venta idVenta;
    private Producto idProducto;

    // Llena "descuento" y "subtotal" usando la edad del cliente.
    public void aplicarDescuentoPorEdad(Integer edad) {
        double bruto = (pu == null ? 0.0 : pu) * (cantidad == null ? 0.0 : cantidad);
        descuento = DescuentoEdad.calcular(bruto, edad);
        subtotal = bruto - descuento;
    }
}