package ca.utoronto.utm.mcs;
import static org.neo4j.driver.Values.parameters;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

public class neo4jDatabase {
  private Driver driver;
  private String uriDb;
  
  public neo4jDatabase() {
    uriDb = "bolt://localhost:7687";
    driver = GraphDatabase.driver(uriDb, AuthTokens.basic("neo4j","1234"));
  }
  
  //s is the node 
  //Actor is the node LABEL
  //actor is the key (aka node has the property of key)
  //$x is the value for the key
  // 1 if successfull, 2 if id or actor name exist, 3 if neither
  public int insertActor(String actor, String actorId) {
    
    try (Session session = driver.session()){
      
        try(Transaction tx = session.beginTransaction()){

          Result actorResult = tx.run("MATCH (a:actor {name:$x}) \n RETURN a.name", parameters("x", actor));
          if(actorResult.hasNext()) {

            session.close();
            return 2 ;
          }
          Result actorIdResult = tx.run("MATCH (a:actor {actorId:$x}) \n RETURN a.actorId", parameters("x", actorId));
          if(actorIdResult.hasNext()) {
            session.close();
            return 2 ;
          }
        
          
        }

        session.writeTransaction(tx -> tx.run("MERGE (a:actor {name: $x , actorId: $y})", 
            parameters("x", actor , "y" , actorId)));
        session.close();
        return 1;
    }
    
  }
  
  public void close() {
    driver.close();
  }
}
