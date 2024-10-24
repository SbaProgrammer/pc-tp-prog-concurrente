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

    
    /* Método para que los instructores esperen en la cabina hasta que los esquiadores estén listos.
    El instructor espera en la cabina hasta que 4 esquiadores estén preparados para la clase.  
    Utiliza un lock y una barrera para sincronizar la llegada de los esquiadores.*/
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

    /*Método para que el instructor imparta la clase, Simula que el instructor da una clase durante 8 segundos y luego libera a los esquiadores. */
    public void darClase(int barreraCorrespondiente) {
        try {
            System.out.println(" [" + Thread.currentThread().getName() + "] dando su Clase  " + (barreraCorrespondiente + 1));
            TimeUnit.SECONDS.sleep(8); // Simula la duración de la clase
            System.out.println(" [" + Thread.currentThread().getName() + "] Terminado su Clase  " + (barreraCorrespondiente + 1));
            postClass[barreraCorrespondiente].release(4);
        } catch (InterruptedException ex) {
            Logger.getLogger(ComplejoInvernal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /* Método para que los esquiadores tomen una clase. Los esquiadores se preparan para la clase y 
    esperan al instructor. Cuando hay suficientes esquiadores, se les asigna una clase. */
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

    /* Método para que el esquiador finalice su clase. Los esquiadores esperan a que termine la clase y son liberados. */
    public void tenerClase(int miClase) {
        try {
            postClass[miClase].acquire();
            System.out.println(" [" + Thread.currentThread().getName() + "]  Termino su clase " + miClase);
        } catch (InterruptedException ex) {
            Logger.getLogger(ComplejoInvernal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /* Método para que los esquiadores accedan a los medios de elevación. Permite que el esquiador use el molinete para acceder a los medios de elevación (sillas). */
    public void accederMedio(int eleccionSilla) {
        medios.get(eleccionSilla).usarMolinete();
    }

    /* Método para acceder a la confitería. Intenta que el esquiador entre a la confitería. Si tiene éxito, retorna true. */
    public boolean accederConfiteria() {
        try {
            return confiteria.entrar();
        } catch (InterruptedException ex) {
            Logger.getLogger(ComplejoInvernal.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    // Método para que los esquiadores se sirvan en la confitería
    public void servirseConfiteria() {
        confiteria.servir();
    }

    // Método para salir de la confitería
    public void salirConfiteria() {
        confiteria.salir();
    }
}