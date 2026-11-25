package pe.edu.upeu.sysventas.model;

// ENCAPSULAMIENTO: la regla del descuento por edad vive en un solo lugar.
// Para cambiar la edad o el porcentaje solo se modifican estas dos constantes.
public final class DescuentoEdad {
    public static final int EDAD_MINIMA = 50;      // desde esta edad (mayores de edad) se aplica el descuento
    public static final double PORCENTAJE = 15.0;  // % de descuento

    private DescuentoEdad() {
    }

    public static boolean aplica(Integer edad) {
        return edad != null && edad >= EDAD_MINIMA;
    }

    // Monto que se descuenta (0 si la edad no califica).
    public static double calcular(double monto, Integer edad) {
        if (!aplica(edad)) return 0.0;
        return Math.round(monto * PORCENTAJE) / 100.0;
    }

    // Monto que queda después de aplicar el descuento.
    public static double montoFinal(double monto, Integer edad) {
        return monto - calcular(monto, edad);
    }
}
