package practica;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.io.*;
import java.util.Arrays;

public class HttpServer {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(37000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 37000.");
            System.exit(1);
        }

        boolean running = true;

        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;

            boolean isFirstLine = true;
            String firstLine = "";

            while ((inputLine = in.readLine()) != null) {
                //System.out.println("Recibí: " + inputLine);
                if (isFirstLine) {
                    firstLine = inputLine;
                    isFirstLine = false;
                }
                if (!in.ready()) {break; }
            }

            System.out.println(firstLine);

            String method = firstLine.split(" ")[0];
            String path = firstLine.split(" ")[1];

            if (path.startsWith("/compreflex")){
                outputLine = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: text/html\r\n"
                        + "\r\n"
                        + extractMethod(path);
            }else {
                outputLine = "HTTP/1.1 400 ERROR\r\n"
                        + "Content-Type: text/html\r\n"
                        + "\r\n";
            }

            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }

        serverSocket.close();
    }

    private static String extractMethod(String path) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String response = "";
        String method = path.split("=")[1].replace(" ", "");

        if (method.startsWith("Class")){
            String className = method.split("\\(")[1].split("\\)")[0];

            Class c = Class.forName(className);

            response = Arrays.toString(c.getDeclaredFields()) + Arrays.toString(c.getDeclaredMethods());
        } else if (method.startsWith("invoke")){
            String className = method.split("\\(")[1].split("\\)")[0].split(",")[0];
            String methodName = method.split("\\(")[1].split("\\)")[0].split(",")[1];

            Class c = Class.forName(className);
            Method m = c.getMethod(methodName);

            response = m.invoke(null).toString();
        } else if (method.startsWith("unaryInvoke")) {
            String className = method.split("\\(")[1].split("\\)")[0].split(",")[0];
            String methodName = method.split("\\(")[1].split("\\)")[0].split(",")[1];
            String paramType = method.split("\\(")[1].split("\\)")[0].split(",")[2];
            String paramValue = method.split("\\(")[1].split("\\)")[0].split(",")[3];

            Class c = Class.forName(className);
            Class p = getParamClass(paramType);
            Method m = c.getMethod(methodName, p);

            response = m.invoke(null, getParamValue(paramType, paramValue)).toString();
        } else if (method.startsWith("binaryInvoke")) {
            String className = method.split("\\(")[1].split("\\)")[0].split(",")[0];
            String methodName = method.split("\\(")[1].split("\\)")[0].split(",")[1];
            String paramType1 = method.split("\\(")[1].split("\\)")[0].split(",")[2];
            String paramValue1 = method.split("\\(")[1].split("\\)")[0].split(",")[3];
            String paramType2 = method.split("\\(")[1].split("\\)")[0].split(",")[4];
            String paramValue2 = method.split("\\(")[1].split("\\)")[0].split(",")[5];

            Class c = Class.forName(className);
            Class p1 = getParamClass(paramType1);
            Class p2 = getParamClass(paramType2);
            Method m = c.getMethod(methodName, p1, p2);

            response = m.invoke(null, getParamValue(paramType1, paramValue1), getParamValue(paramType2, paramValue2)).toString();
        } else{
            response = "<!DOCTYPE html>\n"
                    + "<html>\n"
                    + "<head>\n"
                    + "<meta charset=\"UTF-8\">\n"
                    + "<title>Not Available</title>\n"
                    + "</head>\n"
                    + "<body>\n"
                    + "<h1>Method not Found</h1>\n"
                    + "</body>\n"
                    + "</html>\n";
        }

        return response;
    }

    private static Object getParamValue(String paramType, String paramValue) {
        if (paramType.equals("int")){
            return Integer.valueOf(paramValue);
        } else if (paramType.equals("double")){
            return Double.valueOf(paramValue);
        } else {
            return paramValue;
        }
    }

    private static Class getParamClass(String paramType) {
        if (paramType.equals("int")){
            return int.class;
        } else if (paramType.equals("double")){
            return double.class;
        } else {
            return String.class;
        }
    }
}