package com.servidor.excepciones;

// MaximoContactosAlcanzadoException Lanza esta excepción 
//cuando un vendedor intenta agregar más de 10 contactos a su red.
public class MaximoContactosAlcanzadoException extends Exception {
    public MaximoContactosAlcanzadoException(String mensaje) {
        super("superó la capacidad máxima de contactos en su red");
    }
}