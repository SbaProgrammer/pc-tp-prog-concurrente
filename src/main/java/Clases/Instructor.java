
package Clases;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Instructor extends Persona {

    private final int claseCorresp ;

    public Instructor(String nombre, String apellido, int nroPase, int claseCorrespondiente, ComplejoInvernal complejo) {
        super(nombre, apellido, nroPase, complejo);
        claseCorresp = claseCorrespondiente;
    }

    @Override
    public void run() {
        try {
            // [Instructores] Yendo al cerro
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Instructor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        while (complejo.estaAbierto()) {
            
            // Instructor [Elecciones de Silla y Confiteria]
            int eleccionSilla = random(0, 3);
            
            // Instructor [Accede al Complejo]
            complejo.accederMedio(eleccionSilla);
            
            if (complejo.esperarEnCabina(claseCorresp)) {
                complejo.darClase(claseCorresp);
            }
            
            System.out.println(" [" + Thread.currentThread().getName() + "] Descendiendo Termina su Jornada ");
        }
    }
    
    private int random(int limInferior, int limSuperior) {
        Random random = new Random();
        return random.nextInt((limSuperior - limInferior) + 1) + limInferior;
    }
}

