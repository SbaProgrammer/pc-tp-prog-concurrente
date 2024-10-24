
package Clases;

public class Comida {
    private int costo;
    private String nombre;

    public Comida(int costo, String nombre) {
        this.costo = costo;
        this.nombre = nombre;
    }

    public int getCosto() {
        return costo;
    }

    public void setCosto(int costo) {
        this.costo = costo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "Comida{" +
                "costo=" + costo +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}

