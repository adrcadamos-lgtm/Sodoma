package pe.edu.upeu.sysventas.enums;

public enum TipoComida {
    SNACKS("Snacks"),
    BEBIDAS("Bebidas"),
    COMBOS("Combos"),
    DULCES("Dulces");

    private final String descripcion;

    TipoComida(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return descripcion;
    }
}
