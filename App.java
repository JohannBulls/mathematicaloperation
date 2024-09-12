Voy a proporcionarte todas las clases y códigos para la aplicación distribuida que necesitas, incluyendo el cliente web, la fachada de servicios, el servicio de calculadora, y el archivo `pom.xml` para gestionar el proyecto en Maven. Te mostraré cómo organizar todo para que puedas compilar y ejecutar el proyecto fácilmente.

### Estructura del Proyecto
```bash
├── ReflexCalculator (Servicio de calculadora)
│   ├── src/main/java/com/example/calculator/ReflexCalculator.java
│   └── pom.xml
├── ServiceFacade (Fachada de servicios)
│   ├── src/main/java/com/example/facade/ServiceFacade.java
│   └── pom.xml
└── ClienteWeb (Cliente web - HTML/JS)
    ├── index.html
```

### 1. **Servicio de Calculadora (`ReflexCalculator`)**

Crea un proyecto Maven para el servicio de la calculadora:

#### `pom.xml` de `ReflexCalculator`
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>ReflexCalculator</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <!-- Dependencia para manejar JSON -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.8.8</version>
        </dependency>
        <!-- Servlet API -->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>4.0.1</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Plugin para compilar y ejecutar la aplicación -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.1</version>
            </plugin>
        </plugins>
    </build>
</project>
```

#### `ReflexCalculator.java`
Esta clase implementa la lógica para invocar métodos de la clase `Math` mediante reflexión y para ejecutar el algoritmo de Bubble Sort.

```java
package com.example.calculator;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

public class ReflexCalculator extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("comando");
        String[] parts = command.split("[(),]");
        String operation = parts[0];
        Double[] params = Arrays.stream(parts).skip(1).map(Double::parseDouble).toArray(Double[]::new);
        Double result;
        
        if (operation.equals("bbl")) {
            // Bubble Sort
            result = bubbleSort(params);
        } else {
            // Reflexión en clase Math
            result = invokeMathMethod(operation, params);
        }

        response.setContentType("application/json");
        response.getWriter().write(new Gson().toJson(result));
    }

    private Double invokeMathMethod(String methodName, Double[] params) {
        try {
            Class<?> mathClass = Math.class;
            Method method = mathClass.getMethod(methodName, Double.TYPE);
            return (Double) method.invoke(null, params[0]);
        } catch (Exception e) {
            throw new RuntimeException("Error invocando método " + methodName, e);
        }
    }

    private Double bubbleSort(Double[] numbers) {
        Double[] arr = Arrays.copyOf(numbers, numbers.length);
        boolean swapped;
        do {
            swapped = false;
            for (int i = 0; i < arr.length - 1; i++) {
                if (arr[i] > arr[i + 1]) {
                    Double temp = arr[i];
                    arr[i] = arr[i + 1];
                    arr[i + 1] = temp;
                    swapped = true;
                }
            }
        } while (swapped);
        return arr[0]; // Retornar el primer elemento ordenado
    }
}
```

### 2. **Fachada de Servicios (`ServiceFacade`)**

Crea otro proyecto Maven para la fachada de servicios:

#### `pom.xml` de `ServiceFacade`
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" 
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://www.mavens.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>ServiceFacade</artifactId>
    <version>1.0-SNAPSHOT</version>

    <dependencies>
        <!-- Dependencia para manejar JSON -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.8.8</version>
        </dependency>
        <!-- Servlet API -->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>4.0.1</version>
            <scope>provided</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.3.1</version>
            </plugin>
        </plugins>
    </build>
</project>
```

#### `ServiceFacade.java`
Este es el código para la fachada de servicios que delega las solicitudes al servicio de la calculadora.

```java
package com.example.facade;

import java.io.*;
import java.net.*;
import javax.servlet.http.*;
import com.google.gson.Gson;

public class ServiceFacade extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("comando");

        if (command != null) {
            // Redirigir al servicio de la calculadora
            URL calculatorURL = new URL("http://localhost:8081/compreflex?comando=" + command);
            HttpURLConnection conn = (HttpURLConnection) calculatorURL.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            
            response.setContentType("application/json");
            response.getWriter().write(content.toString());
        } else {
            // Servir el cliente web
            response.setContentType("text/html");
            response.getWriter().write("<html><body><h1>Calculadora</h1> ...cliente HTML...</body></html>");
        }
    }
}
```

### 3. **Cliente Web (HTML + JS)**

El cliente web envía las solicitudes de manera asíncrona a la fachada de servicios. Coloca este archivo en el proyecto `ServiceFacade` y modifícalo si es necesario:

#### `index.html`
```html
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Calculadora Web</title>
</head>
<body>
    <h1>Calculadora</h1>
    <input type="text" id="command" placeholder="Ingresa comando (e.g. sin(0.5))">
    <button onclick="compute()">Calcular</button>

    <div id="result"></div>

    <script>
        function compute() {
            const command = document.getElementById("command").value;
            fetch("/computar?comando=" + encodeURIComponent(command))
                .then(response => response.json())
                .then(data => {
                    document.getElementById("result").innerText = "Resultado: " + data;
                })
                .catch(error => console.error('Error:', error));
        }
    </script>
</body>
</html>
``

`

### 4. **Instrucciones de Despliegue**

1. **Compila y empaqueta ambos proyectos usando Maven**:
   ```bash
   mvn clean package
   ```

2. **Inicia cada servicio en puertos diferentes**:
   - `ReflexCalculator` en el puerto 8081.
   - `ServiceFacade` en el puerto 8080.

3. **Prueba la aplicación**:
   Abre el navegador y visita la URL de la fachada de servicios:
   ```
   http://localhost:8080/calculadora
   ```

Esto debería ser suficiente para que puedas ejecutar y verificar que el sistema distribuido funcione según lo solicitado.