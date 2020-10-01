package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class addRelationship implements HttpHandler {

    private neo4jDatabase dataBase;
    private String actorId;
    private String movieId;

    public addRelationship() {
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
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        if (deserialized.has("movieId") && deserialized.has("actorId")) {
            this.movieId = deserialized.getString("movieId");
            this.actorId = deserialized.getString("actorId");

            int check = dataBase.insertRelation(movieId, actorId);
            if (check == 1) {
                r.sendResponseHeaders(200, -1);
            } else if (check == 2) {
                r.sendResponseHeaders(400, -1);
            } else {
                r.sendResponseHeaders(500, -1);
            }

        } else {
            r.sendResponseHeaders(400, -1);
        }

    }

}
