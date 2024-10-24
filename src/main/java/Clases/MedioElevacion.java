
package Clases;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class MedioElevacion {

    private final AtomicInteger contadorUsos = new AtomicInteger(0);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final int id, num;
    private CyclicBarrier barrera;

    public MedioElevacion(int id, int numMolinete) {
        this.id = id;
        num = numMolinete;
        barrera = new CyclicBarrier(numMolinete, () -> {
            System.out.println(" [ Medio de Elevacion "+id +", con esquiadores Comenzando el viaje ] ");
        });
    }

    public void abrir() {
        scheduler.scheduleAtFixedRate(() -> {System.out.println(" [Complejo] Medio de elevacion " + id + " en funcionamiento."+ "Numero de Molinetes"+ num);}, 0, 1, TimeUnit.HOURS);
    }

    public void cerrar() {
        scheduler.shutdown();
        System.out.println(" [Complejo] Medio de elevacion " + id + " cerrado. Usos totales: " + contadorUsos.get());
    }

    public void usarMolinete() {
        try {
        System.out.println(" [" + Thread.currentThread().getName() + "] accedio a la silla de Medio de elevacion: " + id + ".");            
        barrera.await(5, TimeUnit.SECONDS);  // Timeout de 5 segundos

        System.out.println(" [" + Thread.currentThread().getName() + "] arrancando a subir, Sillas Llenas");

    } catch (TimeoutException e) {
            // Si el primer hilo que llego a la barrera, ya espero 5 segundos entonces se rompe la barrera y se libera a todos los hilos en la barrera
            barrera.reset();
            System.out.println("Medio de Elevacion con sillas no llenas, arrancando");

        } catch (InterruptedException | BrokenBarrierException e) {
            // Si la barrera se rompe, los demás hilos reciben esta excepcion. En este Caso no realizan nada.
        } finally {
            // Incrementa el contador de uso y sube en conjunto con los hilos
            contadorUsos.incrementAndGet();
            System.out.println(" [" + Thread.currentThread().getName() + "]"+ " en [Medio de elevacion: " + id + "] Subiendo");
        }  
    }
}
