package Clases;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

// En Cafeteria Utilizamos Monitores y Metodo Sincronizados 
public class Confiteria {

    private static final int CAPACIDAD = 100;
    private int capacidadDisponible = CAPACIDAD;
    private int plataObtenida = 0;
    private Comida[] menuComidas = {new Comida(10, "hamburguesa"),
        new Comida(5, "choripan"),
        new Comida(10, "pizza"),
        new Comida(15, "guiso"),
        new Comida(20, "tiramisu"),
        new Comida(25, "lemon pie")};

    private int mostradoresComidaDisponibles = 2;
    private int mostradoresPostreDisponibles = 1;

    public synchronized boolean entrar() {
        boolean entro = false;
        if (capacidadDisponible > 0) {
            capacidadDisponible--;
            entro = true;
        }
        return entro;
    }

    private int random(int limInferior, int limSuperior) {
        Random random = new Random();
        return random.nextInt((limSuperior - limInferior) + 1) + limInferior;
    }

    public void servir() {
        try {
            int comida;
            int postre;

            // Seccion sincronizado para la utilizacion de la caja donde se determina que va consumir y se paga
            synchronized (this) {

                comida = random(0, 3);
                postre = random(4, 6); // La opcion Numero 6, hace refencia a que no eligue postre

                plataObtenida = plataObtenida + menuComidas[comida].getCosto();

                // Si es distinto de 6, es porque quiere un postre
                if (postre != 6) {
                    plataObtenida = plataObtenida + menuComidas[postre].getCosto();
                }
            }

            // Bloque sincronizado para los mostradores de comida (Hay 2)
            synchronized (this) {
                while (mostradoresComidaDisponibles == 0) {
                    wait();
                }
                mostradoresComidaDisponibles--;
            }

            try {
                // Tomar comida
                System.out.println(" [Confiteria] [" + Thread.currentThread().getName() + "] tomo su comida.");
            } finally {
                synchronized (this) {
                    mostradoresComidaDisponibles++;
                    notifyAll();
                }
            }

            Thread.sleep(random(1000, 2000));

            if (postre != 6) {
                // Bloque sincronizado para mostradores de postre
                synchronized (this) {
                    while (mostradoresPostreDisponibles == 0) {
                        wait();
                    }
                    mostradoresPostreDisponibles--;
                }

                System.out.println(" [Confiteria] [" + Thread.currentThread().getName() + "] tomó su postre.");

                synchronized (this) {
                    mostradoresPostreDisponibles++;
                    notifyAll();
                }
                
                Thread.sleep(random(1000, 2000));
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Confiteria.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void salir() {
        capacidadDisponible++;
        notifyAll();
    }

    public synchronized int getPlataObtenida() {
        return plataObtenida;
    }
}
