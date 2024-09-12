package co.edu.escuelaing;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ServiceFacade {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(4000)) {
            System.out.println("Fachada lista en el puerto 4000...");
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String inputLine = in.readLine();
                    System.out.println("Recibido en fachada: " + inputLine);

                    if (inputLine.contains("GET /calculadora")) {
                        entregarClienteWeb(out);
                    }
                    else if (inputLine.contains("/computar")) {
                        String comando = inputLine.split(" ")[1].split("\\?")[1];
                        String respuesta = hacerSolicitudCalculadora(comando);
                        out.println("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + respuesta);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void entregarClienteWeb(PrintWriter out) {
        try {
            String htmlResponse = new String(Files.readAllBytes(Paths.get("src/main/resources/web/index.html")));
            out.println("HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n" + htmlResponse);
        } catch (IOException e) {
            e.printStackTrace();
            out.println("HTTP/1.1 500 Internal Server Error\r\n\r\nError al cargar el archivo HTML");
        }
    }

    public static String hacerSolicitudCalculadora(String comando) {
        try {
            URL url = new URL("http://localhost:5000/compreflex=" + comando);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            return content.toString();
        } catch (Exception e) {
            return "{\"error\":\"Error al conectarse a la calculadora.\"}";
        }
    }
}