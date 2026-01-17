package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallbackUsuarioInterface extends Remote {
    
    // ESTA LÍNEA ES LA QUE TE FALTA O ESTÁ MAL ESCRITA:
    void recibirTrino(Trino trino) throws RemoteException;
}