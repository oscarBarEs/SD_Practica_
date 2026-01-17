//Óscar Barquilla Esteban obarquill1@alumno.uned.es

package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallbackUsuarioInterface extends Remote {
    
    void recibirTrino(Trino trino) throws RemoteException;
}