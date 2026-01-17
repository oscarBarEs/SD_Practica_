package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServicioAutenticacionInterface extends Remote {
    
    // Registra un usuario nuevo en el sistema.
    // Retorna true si el registro fue exitoso, false si el nick ya existe.
    boolean registrarUsuario(String nombre, String nick, String password) throws RemoteException;

    // Inicia sesión.
    // IMPORTANTE: Recibe 'cliente' para poder enviarle notificaciones (callbacks) después.
    boolean login(String nick, String password, CallbackUsuarioInterface cliente) throws RemoteException;

    // Cierra la sesión del usuario.
    boolean logout(String nick) throws RemoteException;
}