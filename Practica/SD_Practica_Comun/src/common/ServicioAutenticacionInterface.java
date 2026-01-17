//Óscar Barquilla Esteban obarquill1@alumno.uned.es

package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServicioAutenticacionInterface extends Remote {

    boolean registrarUsuario(String nombre, String nick, String password) throws RemoteException;

    // Inicia sesión.

    boolean login(String nick, String password, CallbackUsuarioInterface cliente) throws RemoteException;

    // Cierra la sesión del usuario.
    boolean logout(String nick) throws RemoteException;
}