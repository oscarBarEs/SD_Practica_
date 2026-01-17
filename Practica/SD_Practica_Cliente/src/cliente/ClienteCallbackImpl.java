package cliente;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import common.CallbackUsuarioInterface;
import common.Trino;

// Esta clase permite que el Cliente reciba mensajes del Servidor automáticamente
public class ClienteCallbackImpl extends UnicastRemoteObject implements CallbackUsuarioInterface {

    private static final long serialVersionUID = 1L;

    public ClienteCallbackImpl() throws RemoteException {
        super();
    }

    @Override
    public void recibirTrino(Trino trino) throws RemoteException {
        // Este método se ejecuta AUTOMÁTICAMENTE cuando alguien envía un trino
        // Lo único que hacemos es imprimirlo bonito por pantalla
        System.out.println("\n------------------------------------------------");
        System.out.println(">>> NUEVO TRINO RECIBIDO <<<");
        System.out.println("De: " + trino.GetNickPropietario());
        System.out.println("Mensaje: " + trino.GetTrino());
        System.out.println("Fecha: " + new java.util.Date(trino.GetTimestamp()));
        System.out.println("------------------------------------------------");
        // Imprimimos un prompt visual para que el usuario sepa que puede seguir escribiendo
        System.out.print("Elija opción: "); 
    }
}