
package Main;

import Clases.ComplejoInvernal;
import Clases.Esquiador;
import Clases.Instructor;
import java.util.ArrayList;
import java.util.List;

public class SimulacionComplejo {
    
    public static void main(String[] args) {
        ComplejoInvernal complejo = new ComplejoInvernal();
        
        List<Thread> threads = new ArrayList<>();

        // Crear y lanzar esquiadores
        for (int i = 0; i < 20; i++) {
            //boolean esPrincipiante = i % 2 == 0;
            Esquiador esquiador = new Esquiador("Esquiador" + i, "Apellido" + i, i, true, complejo);
            Thread threadEsquiador = new Thread(esquiador, "Esquiador " + (i + 1));
            threads.add(threadEsquiador);
            threadEsquiador.start();
        }

        // Crear y lanzar instructores
         for (int i = 1; i <= 5; i++) {
            Instructor instructor = new Instructor("Instructor" + i, "Apellido" + i, i + 50, i, complejo);
            Thread threadInstructor = new Thread(instructor, "Instructor " + (i ));
            threads.add(threadInstructor);
            threadInstructor.start();
        }

        // Esperar a que todos los hilos terminen
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    
    }
    
}