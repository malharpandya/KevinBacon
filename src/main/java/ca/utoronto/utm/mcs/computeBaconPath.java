package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class computeBaconPath implements HttpHandler {

    private neo4jDatabase dataBase;
    private String actorId;

    public computeBaconPath() {
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

                int check = dataBase.getBaconPath(actorId);
                if (check == 1) {
                    JSONObject response = dataBase.getResponse();
                    r.sendResponseHeaders(200, response.toString().length());
                    OutputStream os = r.getResponseBody();
                    os.write(response.toString().getBytes());
                    os.close();
                } else if (check == 2) {
                    r.sendResponseHeaders(400, -1);
                } else if (check == 4) {
                    r.sendResponseHeaders(404, -1);
                } else {
                    System.out.println("500 ERROR");
                    r.sendResponseHeaders(500, -1);
                }

            } else {
                r.sendResponseHeaders(400, -1);
            }


        } catch (Exception e) {
            r.sendResponseHeaders(500, -1);
        }
    }
}
