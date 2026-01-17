//Óscar Barquilla Esteban obarquill1@alumno.uned.es
package cliente;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import common.CallbackUsuarioInterface;
import common.Trino;

public class CallbackUsuarioImpl extends UnicastRemoteObject implements CallbackUsuarioInterface {

    private static final long serialVersionUID = 1L;

    public CallbackUsuarioImpl() throws RemoteException {
        super();
    }

    @Override
    public void recibirTrino(Trino trino) throws RemoteException {

        System.out.println("\n------------------------------------------------");
        System.out.println(">>> NUEVO TRINO RECIBIDO <<<");
        System.out.println("De: " + trino.GetNickPropietario());
        System.out.println("Mensaje: " + trino.GetTrino());
        System.out.println("Fecha: " + new java.util.Date(trino.GetTimestamp()));
        System.out.println("------------------------------------------------");
        System.out.print("Elija opción: "); 
    }
}