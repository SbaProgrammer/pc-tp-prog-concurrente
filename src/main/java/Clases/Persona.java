
package Clases;

public abstract class Persona implements Runnable{
    
    String nombre;
    String apellido;
    int nroPase;
    ComplejoInvernal complejo;
            
    public Persona(String nombre, String apellido, int nroPase, ComplejoInvernal complejo) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.nroPase = nroPase;
        this.complejo = complejo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public int getNroPase() {
        return nroPase;
    }
    
}