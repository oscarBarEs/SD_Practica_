//Óscar Barquilla Esteban obarquill1@alumno.uned.es

package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServicioDatosInterface extends Remote {
    
    // --- Gestión de Usuarios ---
    boolean guardarUsuario(String nombre, String nick, String password) throws RemoteException;
    boolean validarCredenciales(String nick, String password) throws RemoteException;
    
    // --- Gestión de Seguidores ---
    void guardarSeguidor(String nickSeguidor, String nickSeguido) throws RemoteException;
    void eliminarSeguidor(String nickSeguidor, String nickSeguido) throws RemoteException;
    List<String> obtenerSeguidores(String nick) throws RemoteException;
    
    // --- Gestión de Trinos ---
    void guardarTrino(Trino trino) throws RemoteException;
    List<Trino> obtenerTrinosDe(String nick) throws RemoteException;
    boolean eliminarTrino(String nick, long timestamp) throws RemoteException;

    // --- Gestión Offline (Trinos pendientes) ---
    void guardarTrinoPendiente(String nickDestinatario, Trino trino) throws RemoteException;
    List<Trino> obtenerTrinosPendientes(String nickDestinatario) throws RemoteException;
    void borrarTrinosPendientes(String nickDestinatario) throws RemoteException;
    
    // --- Estadísticas y Gestión ---
    int obtenerNumeroUsuarios() throws RemoteException;
    
    List<String> obtenerListadoUsuarios() throws RemoteException;
    
    void banearUsuario(String nick) throws RemoteException;
    void desbanearUsuario(String nick) throws RemoteException;
    boolean estaBaneado(String nick) throws RemoteException;
    

}