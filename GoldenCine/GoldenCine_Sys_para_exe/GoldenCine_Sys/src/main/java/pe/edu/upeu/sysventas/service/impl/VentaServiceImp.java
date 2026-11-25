package pe.edu.upeu.sysventas.service.impl;

import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.model.Comida;
import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.model.VentaDetalleProducto;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.VentaRepository;
import pe.edu.upeu.sysventas.service.IComidaService;
import pe.edu.upeu.sysventas.service.IVentaService;

@RequiredArgsConstructor
public class VentaServiceImp extends CrudGenericoServiceImp<Venta, Long> implements IVentaService {
    private final VentaRepository ventaRepository;
    private final IComidaService comidaService;

    @Override
    protected ICrudGenericoRepository<Venta, Long> getRepo() {
        return ventaRepository;
    }

    @Override
    public Venta registrarVenta(Venta venta) {
        if (venta.getDniruc() == null) {
            throw new RuntimeException("La venta necesita un cliente");
        }
        if (venta.getIdUsuario() == null) {
            throw new RuntimeException("La venta necesita un usuario (inicie sesión)");
        }
        boolean sinFunciones = venta.getDetalleVenta() == null || venta.getDetalleVenta().isEmpty();
        boolean sinComida = venta.getDetalleComida() == null || venta.getDetalleComida().isEmpty();
        if (sinFunciones && sinComida) {
            throw new RuntimeException("La venta no tiene productos");
        }

        // El repositorio valida y descuenta el stock dentro de la misma transacción
        // que guarda la venta y sus detalles.
        if (!sinComida) {
            for (VentaDetalleProducto d : venta.getDetalleComida()) {
                Comida c = comidaService.findByid(d.getIdProducto().getIdComida());
                if (c.getStockDisponible() < d.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente de " + c.getNombre()
                            + " (disponible: " + c.getStockDisponible() + ")");
                }
            }
        }
        return ventaRepository.save(venta);
    }
}
