package Clases;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class MedioElevacion {

    private final AtomicInteger contadorUsos = new AtomicInteger(0);
    private final int id, num;
    private CyclicBarrier barrera;
    private Thread hiloMedioElevacion;
    private boolean enFuncionamiento = false; // Estado del medio de elevació

    public MedioElevacion(int id, int numMolinete) {
        this.id = id;
        num = numMolinete;
        barrera = new CyclicBarrier(numMolinete, () -> {
            System.out.println(" [ Medio de Elevacion " + id + ", con esquiadores. Comenzando el viaje. ] ");
        });
    }
    
    public boolean estaAbierto(){
        return enFuncionamiento;
    }
    
    public void abrir() {
        enFuncionamiento = true;
        System.out.println(" [Complejo] Medio de elevacion " + id
                            + " en funcionamiento. Numero de Molinetes: " + num);
        hiloMedioElevacion = new Thread(() -> {
            try {
                while (enFuncionamiento) {
                    // Esperar 1 hora simulada (5 segundos en lugar de 1 hora real)
                    Thread.sleep(5000); 
                }
            } catch (InterruptedException e) {
                System.out.println(" [Complejo] Medio de elevacion " + id + " interrumpido.");
                Thread.currentThread().interrupt();
            }
        });
        hiloMedioElevacion.start();
    }

    public void cerrar() {
        enFuncionamiento = false; // Detener el ciclo del hilo
        if (hiloMedioElevacion != null) {
            try {
                hiloMedioElevacion.join(); // Esperar a que el hilo termine
                System.out.println(" [Complejo] Medio de elevacion " + id
                        + " cerrado. Usos totales: " + contadorUsos.get());
            } catch (InterruptedException e) {
                System.out.println(" [Complejo] Error al cerrar el medio de elevación " + id);
                Thread.currentThread().interrupt();
            }
        }
    }

    public void usarMolinete() {
        try {
            
            System.out.println(" [" + Thread.currentThread().getName() + "] accedio al molinete del medio de elevacion numero: " + id + ".");
            
            barrera.await(5, TimeUnit.SECONDS);  // Timeout de 5 segundos
            
            System.out.println(" [" + Thread.currentThread().getName() + "] arrancando a subir el medio de elevacion (Sillas llenas).");

        } catch (TimeoutException e) {
            // Si el primer hilo que llego a la barrera, ya espero 5 segundos entonces se rompe la barrera y se libera a todos los hilos en la barrera
            barrera.reset();
            System.out.println("Medio de Elevacion con sillas no llenas, arrancando");

        } catch (InterruptedException | BrokenBarrierException e) {
            // Si la barrera se rompe, los demás hilos reciben esta excepcion. En este Caso no realizan nada.
        } finally {
            // Incrementa el contador de uso y sube en conjunto con los hilos
            contadorUsos.incrementAndGet();
            System.out.println(" [" + Thread.currentThread().getName() + "]" + " en [Medio de elevacion: " + id + "]. Subiendo ^");
        }
    }
}
