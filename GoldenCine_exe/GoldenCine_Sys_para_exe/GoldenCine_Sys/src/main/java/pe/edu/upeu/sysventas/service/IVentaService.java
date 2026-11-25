package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.model.Venta;

public interface IVentaService extends ICrudGenericoService<Venta, Long> {
    // Valida la venta, descuenta el stock de la comida y la guarda.
    Venta registrarVenta(Venta venta);
}
