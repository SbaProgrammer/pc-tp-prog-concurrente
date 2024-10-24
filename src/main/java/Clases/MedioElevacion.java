
package Clases;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MedioElevacion {

    private final Semaphore molinetes;
    private final AtomicInteger contadorUsos = new AtomicInteger(0);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final int id;

    public MedioElevacion(int id, int numMolinete) {
        this.id = id;
        molinetes = new Semaphore(numMolinete);
    }

    public void abrir() {
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println(" [Complejo] Medio de elevacion " + id + " en funcionamiento.");
        }, 0, 1, TimeUnit.HOURS);
    }

    public void cerrar() {
        scheduler.shutdown();
        System.out.println(" [Complejo] Medio de elevacion " + id + " cerrado. Usos totales: " + contadorUsos.get());
    }

    public void usarMolinete() {
        try {
            molinetes.acquire();
            System.out.println(" ["+Thread.currentThread().getName() + "] accedio al medio de elevacion " + id + ".");
            contadorUsos.incrementAndGet();
            TimeUnit.SECONDS.sleep(2); // Simula el tiempo de uso del molinete
            molinetes.release();
        } catch (InterruptedException e) {
        }
    }
}
