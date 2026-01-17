package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServicioGestorInterface extends Remote {
    
    // Envía un trino al servidor para que lo distribuya.
    void enviarTrino(String nick, String mensaje) throws RemoteException;
    
    // Lista todos los usuarios registrados en el sistema.
    List<String> listarUsuarios() throws RemoteException; 
    
    // Permite a un usuario seguir a otro.
    void seguirUsuario(String nickSeguidor, String nickSeguido) throws RemoteException;
    
    // Permite dejar de seguir.
    void dejarDeSeguirUsuario(String nickSeguidor, String nickSeguido) throws RemoteException;

    // Borra un trino propio (según especificación pág 5).
    boolean borrarTrino(String nickPropietario, long timestamp) throws RemoteException; 
}