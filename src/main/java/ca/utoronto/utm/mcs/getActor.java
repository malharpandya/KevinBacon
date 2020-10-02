package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class getActor implements HttpHandler {

    private neo4jDatabase dataBase;
    private String actorId;

    public getActor() {
        this.dataBase = new neo4jDatabase();
    }

    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("GET")) {
                handlePut(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handlePut(HttpExchange r) throws IOException, JSONException {

        try {
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);

            if (deserialized.has("actorId")) {
                this.actorId = deserialized.getString("actorId");

                int check = dataBase.getActor(actorId);
                if (check == 1) {
                	System.out.println("Here");
                	String response = neo4jDatabase.getResponse();
                	r.sendResponseHeaders(200, response.length());
                	OutputStream os = r.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else if (check == 3) {
                	System.out.println("Here2");
                	r.sendResponseHeaders(500, -1);
                } else {
                	r.sendResponseHeaders(404, -1);
                }

            } else {
                r.sendResponseHeaders(400, -1);
            }


        } catch (Exception e) {
        	System.out.println("Here1");
            r.sendResponseHeaders(500, -1);
        }
    }
}
