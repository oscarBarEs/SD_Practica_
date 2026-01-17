package cliente;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

import common.CallbackUsuarioInterface;
import common.ServicioAutenticacionInterface;
import common.ServicioGestorInterface;

public class Usuario {

    // Referencias a los servicios remotos
    private static ServicioAutenticacionInterface authService;
    private static ServicioGestorInterface gestorService;
    
    // Objeto callback (nuestra "oreja")
    private static CallbackUsuarioInterface callbackObj;
    
    // Datos de sesión
    private static String miNick = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            // 1. Localizar el registro RMI
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            
            // 2. Buscar los servicios del Servidor
            authService = (ServicioAutenticacionInterface) registry.lookup("ServicioAutenticacion");
            gestorService = (ServicioGestorInterface) registry.lookup("ServicioGestor");
            
            // 3. Preparar nuestro objeto Callback
            callbackObj = new ClienteCallbackImpl();
            
            System.out.println("--- CLIENTE DE MICROBLOGGING INICIADO ---");
            
            // Bucle del Menú Inicial (Registro/Login)
            boolean salir = false;
            while (!salir) {
                mostrarMenuInicial();
                String opcion = scanner.nextLine();
                
                switch (opcion) {
                    case "1":
                        registrarUsuario();
                        break;
                    case "2":
                        if (hacerLogin()) {
                            // Si el login es correcto, entramos al menú principal
                            bucleMenuPrincipal();
                        }
                        break;
                    case "3":
                        salir = true;
                        System.out.println("Adiós.");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            }
            
            // Cerrar recursos
            System.exit(0);
            
        } catch (Exception e) {
            System.err.println("Error en el Cliente:");
            e.printStackTrace();
        }
    }

    // --- MENÚS Y LÓGICA DE INTERFAZ (Pág. 6) ---

    private static void mostrarMenuInicial() {
        System.out.println("\n=== BIENVENIDO AL SISTEMA ===");
        System.out.println("1.- Registrar un nuevo usuario");
        System.out.println("2.- Hacer login");
        System.out.println("3.- Salir");
        System.out.print("Elija opción: ");
    }

    private static void registrarUsuario() {
        try {
            System.out.println("\n--- REGISTRO ---");
            System.out.print("Introduzca Nombre completo: ");
            String nombre = scanner.nextLine();
            System.out.print("Introduzca Nick (único): ");
            String nick = scanner.nextLine();
            System.out.print("Introduzca Password: ");
            String pass = scanner.nextLine();
            
            boolean ok = authService.registrarUsuario(nombre, nick, pass);
            if (ok) {
                System.out.println(">> Registro EXITOSO. Ahora puede hacer login.");
            } else {
                System.out.println(">> ERROR: El nick ya existe.");
            }
        } catch (Exception e) {
            System.out.println("Error en registro: " + e.getMessage());
        }
    }

    private static boolean hacerLogin() {
        try {
            System.out.println("\n--- LOGIN ---");
            System.out.print("Nick: ");
            String nick = scanner.nextLine();
            System.out.print("Password: ");
            String pass = scanner.nextLine();
            
            // IMPORTANTE: Enviamos 'callbackObj' para que el servidor nos pueda contactar
            boolean ok = authService.login(nick, pass, callbackObj);
            
            if (ok) {
                miNick = nick;
                System.out.println(">> Login CORRECTO. Bienvenido " + miNick);
                return true;
            } else {
                System.out.println(">> Credenciales incorrectas o usuario ya conectado.");
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error en login: " + e.getMessage());
            return false;
        }
    }

    // Bucle del Menú Principal (Usuario Logueado) - Pág 6
    private static void bucleMenuPrincipal() {
        boolean logout = false;
        while (!logout) {
            try {
                System.out.println("\n=== MENÚ USUARIO (" + miNick + ") ===");
                System.out.println("1.- Información del Usuario");
                System.out.println("2.- Enviar Trino");
                System.out.println("3.- Listar Usuarios del Sistema");
                System.out.println("4.- Seguir a");
                System.out.println("5.- Dejar de seguir a");
                System.out.println("6.- Borrar trino (Opcional - No implementado)");
                System.out.println("7.- Salir \"Logout\"");
                System.out.print("Elija opción: ");
                
                String opcion = scanner.nextLine();
                
                switch (opcion) {
                    case "1":
                        // Información básica (Pág 6: Info del Usuario)
                        System.out.println("Usuario conectado: " + miNick);
                        break;
                        
                    case "2":
                        System.out.print("Escribe tu mensaje: ");
                        String mensaje = scanner.nextLine();
                        gestorService.enviarTrino(miNick, mensaje);
                        System.out.println(">> Trino enviado.");
                        break;
                        
                    case "3":
                        List<String> usuarios = gestorService.listarUsuarios();
                        System.out.println("--- Usuarios del Sistema ---");
                        // NOTA: Si dejaste el 'throw Exception' en el servidor, esto fallará. 
                        // Asumimos que lo arreglaremos o mostrará error controlado.
                        for (String u : usuarios) {
                            System.out.println("- " + u);
                        }
                        break;
                        
                    case "4":
                        System.out.print("Nick del usuario a seguir: ");
                        String aSeguir = scanner.nextLine();
                        gestorService.seguirUsuario(miNick, aSeguir);
                        System.out.println(">> Ahora sigues a " + aSeguir);
                        break;
                        
                    case "5":
                        System.out.print("Nick del usuario a dejar de seguir: ");
                        String noSeguir = scanner.nextLine();
                        gestorService.dejarDeSeguirUsuario(miNick, noSeguir);
                        System.out.println(">> Dejaste de seguir a " + noSeguir);
                        break;
                        
                    case "6":
                        System.out.println("Opción opcional no implementada.");
                        break;
                        
                    case "7":
                        authService.logout(miNick);
                        miNick = null;
                        logout = true;
                        System.out.println(">> Sesión cerrada.");
                        break;
                        
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (Exception e) {
                // Capturamos excepciones para no sacar al usuario del programa por un error de red puntual
                System.err.println("Error en operación: " + e.getMessage());
            }
        }
    }
    
}