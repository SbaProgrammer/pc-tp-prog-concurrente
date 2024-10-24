
package Clases;

import java.util.Random;

public class Esquiador extends Persona {

    private final boolean esPrincipiante;

    public Esquiador(String nombre, String apellido, int nroPase, boolean esPrincipiante, ComplejoInvernal complejo) {
        super(nombre, apellido, nroPase, complejo);
        this.esPrincipiante = esPrincipiante;
    }

    private int random(int limInferior, int limSuperior) {
        Random random = new Random();
        return random.nextInt((limSuperior - limInferior) + 1) + limInferior;
    }

    @Override
    public void run() {
        // Esquiador [ Se fija si esta el Complejo Abierto]
        if(complejo.estaAbierto()) {
             
            // Esquiador [Elecciones de Silla y Confiteria]
            int eleccionSilla = random(0, 3);
            int eleccionConfiteria = random(0, 1);

            // Esquiador [Accede al Complejo]
            complejo.accederMedio(eleccionSilla);

            // Esquiador [ Si es principiante Toma clases]
            if (esPrincipiante) {
                int miClase = complejo.preClase();
                if (miClase>0) {
                    complejo.tenerClase(miClase);
                } else{
                    System.out.println(" [" + Thread.currentThread().getName() + "] Obtuvo su dinero otra vez, pues no se cumplio la condicion de la Clase");
                }
            }
            /*
            // Esquiador [Esquia]
            if (eleccionConfiteria == 1) {
                // Esquiador [Si hay lugar en la Confiteria accede]
                if (complejo.accederConfiteria()) {
                    complejo.servirseConfiteria();
                    complejo.salirConfiteria();
                }
            }
            
            //Simular tiempo en que esquia
            
            // Aca deberia salir del complejo */
        }
    }
}
