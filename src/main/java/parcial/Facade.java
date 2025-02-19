package parcial;

import java.net.*;
import java.io.*;
import java.io.FileReader;


public class Facade {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String GET_URL = "http://localhost:37000";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(36000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 36000.");
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
                if (!in.ready()) {
                    break;
                }
            }
            System.out.println(firstLine);

            String path = firstLine.split(" ")[1];
            String method = firstLine.split(" ")[0];


            if (path.startsWith("/cliente")){
                outputLine = readStaticFile();
            } else if (path.startsWith("/consulta")) {
                outputLine = connection(path, method);
            } else {
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

    private static String readStaticFile() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader("src/main/resources/index.html"));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine + "\n");
        }
        in.close();
        //System.out.println(response.toString());
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                +response.toString();
    }


    public static String connection(String path, String method) throws IOException {
        URL obj = new URL(GET_URL + path.replace("consulta", "compreflex"));
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("User-Agent", USER_AGENT);
//The following invocation perform the connection implicitly before getting the code
        int responseCode = con.getResponseCode();
        System.out.println("GET Response Code :: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            //System.out.println(response.toString());
            return "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/html\r\n"
                    + "\r\n"
                    +response.toString();
// print result
        } else {
            System.out.println("GET request not worked");
            return "HTTP/1.1 400 ERROR\r\n"
                    + "Content-Type: text/html\r\n"
                    + "\r\n";
        }
    }
}
