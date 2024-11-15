package com.servidor.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.concurrent.locks.ReentrantLock;

import com.servidor.modelo.MarketPlace;
import com.servidor.modelo.Producto;
import com.servidor.modelo.Solicitud;
import com.servidor.modelo.Vendedor;

public class UtilSerializar implements Serializable {
    private static UtilSerializar instancia;
    private UtilProperties utilProperties;
    private UtilLog utilLog;
    private UtilPersistencia utilPersistencia;
    private final ReentrantLock lock = new ReentrantLock();
 // constructor 
    private UtilSerializar() {
        this.utilProperties = UtilProperties.getInstance();
        this.utilLog = UtilLog.getInstance();
        this.utilPersistencia = UtilPersistencia.getInstance();
    }

    // Método para guardar el modelo serializado en un único archivo
    public void guardarModeloSerializadoBin(Object modelo) {
        lock.lock();
        try {
            String rutaArchivo = utilProperties.obtenerPropiedad("rutaModeloSerializado.bin");
            SerializarTarea tarea = new SerializarTarea(modelo, rutaArchivo, false);
            tarea.start();
            tarea.join(); // Espera a que la tarea termine
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
            utilLog.escribirLog("La tarea de serialización fue interrumpida: " + e.getMessage(), Level.SEVERE);
        } finally {
            lock.unlock();
        }
    }
 // se crea la unica instancia de la clase 
    public static UtilSerializar getInstance() {
        if (instancia == null) {
            instancia = new UtilSerializar();
        }
        return instancia;
    }
 // metodo que serializa y guarda los objetos de tipo marketplace 
 public void guardarModeloSerializadoXML(MarketPlace modelo) {
    lock.lock();
    try {
        String rutaArchivo = utilProperties.obtenerPropiedad("rutaModeloSerializado.xml");
        SerializarTarea tarea = new SerializarTarea(modelo, rutaArchivo, true);
        tarea.start();
        tarea.join(); // Espera a que la tarea termine
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
        utilLog.escribirLog("La tarea de serialización fue interrumpida: " + e.getMessage(), Level.SEVERE);
    } finally {
        lock.unlock();
    }
}

    // Método para cargar el modelo desde el archivo
    public Object cargarModeloSerializadoBin() {
        lock.lock();
        try {
            String rutaArchivo = utilProperties.obtenerPropiedad("rutaModeloSerializado.bin");
            List<Object> lista = new ArrayList<>();
            new DeserializarTarea(rutaArchivo, false, lista).start();
            // Aquí deberías implementar algún mecanismo para esperar a que la tarea termine
            // Por simplicidad, se retorna el primer elemento de la lista
            return lista.isEmpty() ? null : lista.get(0);
        } finally {
            lock.unlock();
        }
    }
 // metodo que carga los obe
    public MarketPlace cargarModeloSerializadoDesdeXML() {
        lock.lock();
        try {
            String rutaArchivo = utilProperties.obtenerPropiedad("rutaModeloSerializado.xml");
            List<Object> lista = new ArrayList<>();
            new DeserializarTarea(rutaArchivo, true, lista).start();
            // Aquí deberías implementar algún mecanismo para esperar a que la tarea termine
            // Por simplicidad, se retorna el primer elemento de la lista
            return (MarketPlace) (lista.isEmpty() ? null : lista.get(0));
        } finally {
            lock.unlock();
        }
    }

    public void actualizarSerializacionVendedores() {
        lock.lock();
        try {
            List<Vendedor> listaVendedores = utilPersistencia.leerVendedoresDesdeArchivo();
            serializarLista(listaVendedores, true);
            serializarLista(listaVendedores, false);
            utilLog.escribirLog("Serialización de vendedores actualizada correctamente.", Level.INFO);
        } finally {
            lock.unlock();
        }
    }

    public void actualizarSerializacionProductos() {
        lock.lock();
        try {
            List<Producto> listaProductos = utilPersistencia.leerProductosDesdeArchivo();
            serializarLista(listaProductos, false);
            serializarLista(listaProductos, true);
            utilLog.escribirLog("Serialización de productos actualizada correctamente.", Level.INFO);
        } finally {
            lock.unlock();
        }
    }

    public void actualizarSerializacionSolicitudes() {
        lock.lock();
        try {
            List<Solicitud> listaSolicitudes = utilPersistencia.leerSolicitudesDesdeArchivo();
            serializarLista(listaSolicitudes, false);
            serializarLista(listaSolicitudes, true);
            utilLog.escribirLog("Serialización de solicitudes actualizada correctamente.", Level.INFO);
        } finally {
            lock.unlock();
        }
    }

    public void serializarLista(List<?> lista, boolean esXML) {
        lock.lock();
        try {
            String ruta;
            if (lista.isEmpty()) {
                utilLog.escribirLog("La lista está vacía, no se puede serializar.", Level.WARNING);
                return;
            }

            Object primerElemento = lista.get(0);
            if (primerElemento instanceof Producto) {
                ruta = esXML ? utilProperties.obtenerPropiedad("rutaProductos.xml")
                        : utilProperties.obtenerPropiedad("rutaProductos.bin");
            } else if (primerElemento instanceof Vendedor) {
                ruta = esXML ? utilProperties.obtenerPropiedad("rutaVendedores.xml")
                        : utilProperties.obtenerPropiedad("rutaVendedores.bin");
            } else if (primerElemento instanceof Solicitud) {
                ruta = esXML ? utilProperties.obtenerPropiedad("rutaSolicitudes.xml")
                        : utilProperties.obtenerPropiedad("rutaSolicitudes.bin");
            } else {
                utilLog.escribirLog("No se puede serializar la lista, tipo de objeto desconocido.", Level.SEVERE);
                return;
            }

            new SerializarTarea(lista, ruta, esXML).start();
        } finally {
            lock.unlock();
        }
    }

    public void serializarObjeto(Object obj, String ruta, boolean esXML) {
        lock.lock();
        try {
            new SerializarTarea(obj, ruta, esXML).start();
        } finally {
            lock.unlock();
        }
    }

    public List<Producto> deserializarProductos(boolean esXML) {
        lock.lock();
        try {
            String ruta = esXML ? utilProperties.obtenerPropiedad("rutaProductos.xml")
                    : utilProperties.obtenerPropiedad("rutaProductos.bin");
            List<Object> lista = new ArrayList<>();
            new DeserializarTarea(ruta, esXML, lista).start();
            List<Producto> productos = new ArrayList<>();
            for (Object obj : lista) {
                productos.add((Producto) obj);
            }
            return productos;
        } finally {
            lock.unlock();
        }
    }

    public List<Vendedor> deserializarVendedores(boolean esXML) {
        lock.lock();
        try {
            String ruta = esXML ? utilProperties.obtenerPropiedad("rutaVendedores.xml")
                    : utilProperties.obtenerPropiedad("rutaVendedores.bin");
            List<Object> lista = new ArrayList<>();
            new DeserializarTarea(ruta, esXML, lista).start();
            List<Vendedor> vendedores = new ArrayList<>();
            for (Object obj : lista) {
                vendedores.add((Vendedor) obj);
            }
            return vendedores;
        } finally {
            lock.unlock();
        }
    }

    public List<Solicitud> deserializarSolicitudes(boolean esXML) {
        lock.lock();
        try {
            String ruta = esXML ? utilProperties.obtenerPropiedad("rutaSolicitudes.xml")
                    : utilProperties.obtenerPropiedad("rutaSolicitudes.bin");
            List<Object> lista = new ArrayList<>();
            new DeserializarTarea(ruta, esXML, lista).start();
            List<Solicitud> solicitudes = new ArrayList<>();
            for (Object obj : lista) {
                solicitudes.add((Solicitud) obj);
            }
            return solicitudes;
        } finally {
            lock.unlock();
        }
    }

}
