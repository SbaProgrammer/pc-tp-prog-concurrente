/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Clases;

// Clase principal del complejo invernal

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ComplejoInvernal {

    private static final int NUM_BARRERAS = 5; // Número de barreras (clases concurrentes)
    private final List<CyclicBarrier> barrerasClases = new ArrayList<>();

    private final List<MedioElevacion> medios = new ArrayList<>();
    private static final int HORA_APERTURA = 10, HORA_CIERRE = 17;
    private final AtomicInteger cantEsperandoClase = new AtomicInteger(0);
    private final AtomicInteger barreraLibre = new AtomicInteger(0);
    private final AtomicInteger horaActual = new AtomicInteger(9);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private final Lock accesoClase = new ReentrantLock(true);
    private final Condition esperaCabina = accesoClase.newCondition();  // Usamos la condición ya existente
    private final Semaphore esquiadoresListos = new Semaphore(0, true), instructorAvisaProximoInstructor = new Semaphore(0); // Controla cuándo los esquiadores pueden avanzar
    private final Semaphore postClass[] = {new Semaphore(0, true),new Semaphore(0, true),new Semaphore(0, true),new Semaphore(0, true),new Semaphore(0, true)};
    private final Confiteria confiteria = new Confiteria();

    // Constructor
    public ComplejoInvernal() {
        // Crear 4 medios de elevación con 1, 2, 3 y 4 molinetes respectivamente
        for (int i = 1; i <= 4; i++) {
            medios.add(new MedioElevacion(i, i));
        }

        // Crear barreras para cada clase concurrente
        for (int i = 0; i < NUM_BARRERAS; i++) {
            final int claseId = i;
            barrerasClases.add(new CyclicBarrier(5, new Runnable() {
                @Override
                public void run() {
                        System.out.println(" [Complejo] Clase " + (claseId + 1) + ": Instructor y esquiadores LISTOS para la clase.");
                }
            }));
            //semaforosListos.add(new Semaphore(0)); // Semáforo para cada barrera
        }

        // Iniciar Simulación
        scheduler.scheduleAtFixedRate(() -> {
            int hora = horaActual.incrementAndGet();
            System.out.println(" [Complejo] Hora actual: " + hora + ":00");
            if (hora == HORA_APERTURA) {
                System.out.println(" [Complejo] Medios de elevación abiertos.");
                for (MedioElevacion medio : medios) {
                    medio.abrir();
                }
            } else if (hora == HORA_CIERRE) {
                cerrarMediosElevacion();
                scheduler.shutdown(); // Detiene el scheduler después de cerrar los medios
                System.out.println(" [Complejo] Simulación finalizada.");
            }
        }, 0, 5, TimeUnit.SECONDS); // Simula una hora por segundo para rapidez
    }

    // Método para cerrar los medios de elevación
    private void cerrarMediosElevacion() {
        System.out.println(" [Complejo] Medios de elevación cerrados.");
        for (MedioElevacion medio : medios) {
            medio.cerrar();
        }
    }
    
    /* Método para verificar si el complejo está abierto. Devuelve verdadero si la hora actual está dentro del horario de apertura y cierre del complejo. */
    public boolean estaAbierto() {
        return horaActual.get() >= HORA_APERTURA && horaActual.get() < HORA_CIERRE;
    }

    
    // Metodos relacionados a acciones que toma el "Instructor"
    public void esperarEnCabina(int barreraCorrespondiente) {
        try {
            accesoClase.lock();
            System.out.println(" [" + Thread.currentThread().getName() + "] Esperando en Cabina.");

            // Espera hasta que haya 4 esquiadores listos o se cierre el complejo
            while ((cantEsperandoClase.get() < 4 || barreraLibre.get() != 0) && estaAbierto()) {
                esperaCabina.await(14, TimeUnit.SECONDS); // Espera por un tiempo limitado

                if (cantEsperandoClase.get() < 4 && cantEsperandoClase.get() >= 1 && barreraLibre.get() == 0) {
                    esquiadoresListos.release(cantEsperandoClase.get());
                }
            }

            barreraLibre.set(barreraCorrespondiente);
            esquiadoresListos.release(4); // Permite que los esquiadores avancen
            
            instructorAvisaProximoInstructor.acquire(4);
            cantEsperandoClase.addAndGet(-4);
            barreraLibre.set(0);
            if (cantEsperandoClase.get() >= 4) {
                esperaCabina.signal();
            }
            
        } catch (InterruptedException ex) {
            Logger.getLogger(ComplejoInvernal.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            try {
                accesoClase.unlock();
                barrerasClases.get(barreraCorrespondiente).await(); // El instructor espera en la barrera
            } catch (InterruptedException ex) {
                Logger.getLogger(ComplejoInvernal.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BrokenBarrierException ex) {
                Logger.getLogger(ComplejoInvernal.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void darClase(int barreraCorrespondiente) {
        try {
            
            System.out.println(" [" + Thread.currentThread().getName() + "] dando su Clase  " + (barreraCorrespondiente + 1));
            TimeUnit.SECONDS.sleep(8);
            System.out.println(" [" + Thread.currentThread().getName() + "] Terminado su Clase  " + (barreraCorrespondiente + 1));
            postClass[barreraCorrespondiente].release(4);
            
        } catch (InterruptedException ex) {
            Logger.getLogger(ComplejoInvernal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Metodos relacionados a acciones que toma el "Esquiador"
    public int preClase() {
        int claseCorrespondiente = -1;
        try {
            accesoClase.lock();
            try {
                cantEsperandoClase.incrementAndGet();
                System.out.println(" [" + Thread.currentThread().getName() + "] Esperando para la clase. Esquiador nro: " + cantEsperandoClase.get());

                // Verificación de esquiadores suficientes: Si hay suficientes esquiadores, despierta al instructor
                if (cantEsperandoClase.get() >= 4) {
                    esperaCabina.signalAll();
                }
                
            } finally {
                accesoClase.unlock();
            }

            esquiadoresListos.acquire(); // Aca esperan los esquiadores
            
            if (barreraLibre.get() != 0) {
                // Asigna la clase correspondiente y espera junto al instructor
                instructorAvisaProximoInstructor.release();
                claseCorrespondiente = barreraLibre.get();
                
                barrerasClases.get(claseCorrespondiente).await();
            }

        } catch (InterruptedException | BrokenBarrierException ex) {
            Logger.getLogger(ComplejoInvernal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return claseCorrespondiente;
    }

    public void tenerClase(int miClase) {
        try {
            postClass[miClase].acquire();
            System.out.println(" [" + Thread.currentThread().getName() + "]  Termino su clase " + miClase);
        } catch (InterruptedException ex) {
            Logger.getLogger(ComplejoInvernal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    //Metodo relacionado a los Medios de Elevacion
    public void accederMedio(int eleccionSilla) {
        medios.get(eleccionSilla).usarMolinete();
    }

    // Metodos relacionados a la "Cafeteria"
    public boolean accederConfiteria() {
        return confiteria.entrar();
    }
    
    public void servirseConfiteria() {
        confiteria.servir();
    }

    public void salirConfiteria() {
        confiteria.salir();
    }
}