package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class addActor implements HttpHandler {

    private neo4jDatabase dataBase;
    private String actor;
    private String actorId;

    public addActor() {
        this.dataBase = new neo4jDatabase();
    }

    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("PUT")) {
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

            if (deserialized.has("name") && deserialized.has("actorId")) {
                this.actor = deserialized.getString("name");
                this.actorId = deserialized.getString("actorId");
                r.sendResponseHeaders(dataBase.insertActor(actor, actorId), -1);

            } else {
                r.sendResponseHeaders(400, -1);
            }

        } catch (Exception e) {
            r.sendResponseHeaders(400, -1);
        }
    }
}
