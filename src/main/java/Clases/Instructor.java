
package Clases;

public class Instructor extends Persona {

    private final int claseCorresp ;

    public Instructor(String nombre, String apellido, int nroPase, int claseCorrespondiente, ComplejoInvernal complejo) {
        super(nombre, apellido, nroPase, complejo);
        claseCorresp = claseCorrespondiente;
    }

    @Override
    public void run() {
        while (complejo.estaAbierto()) {
            //System.out.println("PASO");
            complejo.esperarEnCabina(claseCorresp);
            complejo.darClase(claseCorresp);
            // Aca deberia salir del complejo
        }
    }
}
