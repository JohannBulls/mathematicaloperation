package co.edu.escuelaing;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ReflexCalculator {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Calculadora lista en el puerto 5000...");
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                    String inputLine = in.readLine();
                    System.out.println("Recibido: " + inputLine);
                    String[] parts = inputLine.split("=");
                    String comando = parts[1].split("\\(")[0];
                    String[] params = parts[1].split("\\(")[1].replace(")", "").split(",");

                    Double[] valores = Arrays.stream(params).map(Double::parseDouble).toArray(Double[]::new);
                    String resultado;

                    if (comando.equals("bbl")) {
                        bubbleSort(valores);
                        resultado = Arrays.toString(valores);
                    } else {
                        resultado = invocarMetodo(comando, valores);
                    }

                    String jsonResponse = "{\"resultado\":\"" + resultado + "\"}";
                    out.println("HTTP/1.1 200 OK\r\nContent-Type: application/json\r\n\r\n" + jsonResponse);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String invocarMetodo(String metodo, Double[] parametros) {
        try {
            Class<?> mathClass = Math.class;
            Method metodoRef = mathClass.getMethod(metodo, double.class);
            return String.valueOf(metodoRef.invoke(null, parametros[0]));
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public static void bubbleSort(Double[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                if (arr[j] > arr[j + 1]) {
                    double temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
}

