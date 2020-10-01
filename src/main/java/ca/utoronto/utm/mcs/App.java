package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class App 
{
    static int PORT = 8080;
    public static void main(String[] args) throws IOException
    {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        
        

        server.createContext("/api/v1/addActor", new addActor());
        server.createContext("/api/v1/addMovie", new addMovie());
        //7.1 
        //server.createContext("/api/v1/addActor",);
        server.start();
        System.out.printf("Server started on podrt %d...\n", PORT);
    }
}
