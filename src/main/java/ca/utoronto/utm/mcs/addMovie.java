package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class addMovie implements HttpHandler {

  private neo4jDatabase dataBase;
  private String movie;
  private String movieId;

  public addMovie() {
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
  
      if (deserialized.has("name") && deserialized.has("movieId")) {
        this.movie = deserialized.getString("name");
        this.movieId = deserialized.getString("movieId");
  
        int check = dataBase.insertMovie(movie, movieId);
        if (check == 1) {
          r.sendResponseHeaders(200, -1);
        } else if (check == 2) {
          r.sendResponseHeaders(400, -1);
        } else {
          r.sendResponseHeaders(500, -1);
        }
  
      }
      else {
        r.sendResponseHeaders(400, -1);
      }
     } catch (Exception e) {
       r.sendResponseHeaders(500, -1);
     }
  }
}
