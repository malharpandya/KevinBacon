package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class getMovie implements HttpHandler {

    private neo4jDatabase dataBase;
    private String movieId;

    public getMovie() {
        this.dataBase = new neo4jDatabase();
    }

    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleGet(HttpExchange r) throws IOException, JSONException {
        try {
            System.out.println("1");
            String body = Utils.convert(r.getRequestBody());
            JSONObject deserialized = new JSONObject(body);

            if (deserialized.has("movieId")) {
                this.movieId = deserialized.getString("movieId");

                int check = dataBase.getMovie(movieId);
                if (check == 200) {
                    JSONObject response = dataBase.getResponse();
                    r.sendResponseHeaders(200, response.toString().length());
                    OutputStream os = r.getResponseBody();
                    os.write(response.toString().getBytes());
                    os.close();
                } else {
                    r.sendResponseHeaders(check, -1);
                }

            } else {
                r.sendResponseHeaders(400, -1);
            }


        } catch (Exception e) {
            r.sendResponseHeaders(400, -1);
        }
    }
}
