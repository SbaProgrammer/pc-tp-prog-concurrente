/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Clases;


import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Confiteria {

    private static final int CAPACIDAD = 100;
    private Semaphore capacidad;
    private int plataObtenida = 0;
    private Semaphore caja = new Semaphore(1, true);
    private Semaphore mostradoresComida = new Semaphore(2);
    private Semaphore mostradoresPostre = new Semaphore(1);
    private Comida[] menuComidas = {new Comida(10, "hamburguesa"),
        new Comida(5, "choripan"),
        new Comida(10, "pizza"),
        new Comida(15, "guiso"),
        new Comida(20, "tiramisu"),
        new Comida(25, "lemon pie")};

    public Confiteria() {
        this.capacidad = new Semaphore(CAPACIDAD, true);
    }

    public boolean entrar() throws InterruptedException {
        return capacidad.tryAcquire();
    }

    private int random(int limInferior, int limSuperior) {
        Random random = new Random();
        return random.nextInt((limSuperior - limInferior) + 1) + limInferior;
    }

    public void servir() {
        try {
            int comida;
            int postre;
            
            caja.acquire();
            try {
                // Hace pedido
                comida = random(0, 3);
                postre = random(4, 5);
                
                // Pagando en caja...
                plataObtenida += menuComidas[comida].getCosto();
                
                // Si postre no está incluido, no se paga por postre.
                if (postre != 5) {
                    plataObtenida += menuComidas[postre].getCosto();
                }
            } finally {
                caja.release();
            }
            
            mostradoresComida.acquire();
            try {
                // Tomar comida
                System.out.println(" [Confiteria] ["+Thread.currentThread().getName() + "] tomó su comida.");
            } finally {
                mostradoresComida.release();
            }
            
            // Comer la comida
            Thread.sleep(random(1000, 2000));
            
            if (postre != 5) {
                mostradoresPostre.acquire();
                try {
                    // Tomar postre
                    System.out.println(" [Confiteria] ["+Thread.currentThread().getName() + "]  tomó su postre.");
                } finally {
                    mostradoresPostre.release();
                }
                
                // Comer el postre
                Thread.sleep(random(1000, 2000));
            }
        }   catch (InterruptedException ex) {
            Logger.getLogger(Confiteria.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void salir() {
        capacidad.release();
    }

    public int getPlataObtenida() {
        return plataObtenida;
    }
}
