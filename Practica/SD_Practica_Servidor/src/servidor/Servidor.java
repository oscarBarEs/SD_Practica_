package servidor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import common.ServicioDatosInterface;

// CLASE PRINCIPAL: Arranca el servidor y conecta con la BD
public class Servidor {

    public static void main(String[] args) {
        try {
            System.out.println("Iniciando Servidor...");

            // 1. Localizar el registro RMI (donde vive la Base de Datos)
            // Asumimos localhost y puerto 1099
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            
            // 2. Buscar ("Lookup") el objeto remoto de la Base de Datos
            System.out.println("Buscando servicio de Datos...");
            ServicioDatosInterface db = (ServicioDatosInterface) registry.lookup("ServicioDatos");
            System.out.println("Conexión con BD establecida.");
            
            // 3. Crear la instancia de nuestra lógica (Gestor), pasándole la BD
            ServicioGestorImpl gestor = new ServicioGestorImpl(db);
            
            // 4. Publicar nuestros servicios para que el Cliente los vea
            // Usamos 'rebind' para registrar el MISMO objeto con dos nombres (según pide la arquitectura)
            registry.rebind("ServicioAutenticacion", gestor);
            registry.rebind("ServicioGestor", gestor);
            
            System.out.println("--- SERVIDOR DE NEGOCIO LISTO ---");
            
        } catch (Exception e) {
            System.err.println("Error al arrancar el Servidor:");
            e.printStackTrace();
        }
    }
}