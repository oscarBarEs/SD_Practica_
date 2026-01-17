//Óscar Barquilla Esteban obarquill1@alumno.uned.es

package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServicioGestorInterface extends Remote {
    
    void enviarTrino(String nick, String mensaje) throws RemoteException;
    
    List<String> listarUsuarios() throws RemoteException; 
    
    void seguirUsuario(String nickSeguidor, String nickSeguido) throws RemoteException;
    
    void dejarDeSeguirUsuario(String nickSeguidor, String nickSeguido) throws RemoteException;

    boolean borrarTrino(String nickPropietario, long timestamp) throws RemoteException; 
    
    int obtenerNumSeguidores(String nick) throws RemoteException;
    int obtenerNumTrinos(String nick) throws RemoteException;
    
    List<Trino> listarTrinos(String nick) throws RemoteException;
}