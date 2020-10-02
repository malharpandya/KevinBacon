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
        driver = GraphDatabase.driver(uriDb, AuthTokens.basic("neo4j", "1234"));
    }

    // s is the node
    // Actor is the node LABEL
    // actor is the key (aka node has the property of key)
    // $x is the value for the key
    // 1 if successfull, 2 if id or actor name exist, 3 if neither
    public int insertActor(String actor, String actorId) {

        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {

                Result actorResult = tx.run("MATCH (a:actor {name:$x}) \n RETURN a.name", parameters("x", actor));
                if (actorResult.hasNext()) {

                    session.close();
                    return 2;
                }

                Result actorIdResult =
                        tx.run("MATCH (a:actor {actorId:$x}) \n RETURN a.actorId", parameters("x", actorId));
                if (actorIdResult.hasNext()) {
                    session.close();
                    return 2;
                }

            } catch (Exception e) {
                return 3;
            }

            try {
                session.writeTransaction(
                        tx -> tx.run("MERGE (a:actor {name: $x , actorId: $y})", parameters("x", actor, "y", actorId)));
                session.close();
                return 1;
            } catch (Exception e) {
                return 3;
            }

        } catch (Exception e) {
            return 3;
        }
    }

    public int insertMovie(String movie, String movieId) {

        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {

                Result movieResult = tx.run("MATCH (a:movie {name:$x}) \n RETURN a.name", parameters("x", movie));
                if (movieResult.hasNext()) {

                    session.close();
                    return 2;
                }

                Result movieIdResult =
                        tx.run("MATCH (a:movie {movieId:$x}) \n RETURN a.movieId", parameters("x", movieId));
                if (movieIdResult.hasNext()) {
                    session.close();
                    return 2;
                }


            } catch (Exception e) {
                session.close();
                return 3;
            }

            try {
                session.writeTransaction(
                        yx -> yx.run("MERGE (a:movie {name: $x , movieId: $y})", parameters("x", movie, "y", movieId)));
                session.close();
                return 1;
            } catch (Exception e) {
                return 3;
            }

        } catch (Exception e) {
            return 3;
        }
    }

    // MALHAR WORK ON THIS.
    public int insertRelation(String movieId, String actorId) {

        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                
                // Actor or Movie does not exist
                Result actorExist = tx.run("MATCH (a:actor {actorId:$x}) \n RETURN a.actorId", parameters("x", actorId));
                Result movieExist = tx.run("MATCH (a:movie {movieId:$x}) \n RETURN a.movieId", parameters("x", movieId));
                
                if(!(actorExist.hasNext() && movieExist.hasNext())) {
                    System.out.println("here1");
                    session.close();
                    return 2;
                }

                // Actor and Movie already have a relation

                Result relationExist = tx.run("MATCH (a:actor {actorId:$x}), (b:movie {movieId:$y}) \n MATCH (a)-[r:ACTED_IN]->(b) \n RETURN r", parameters("x", actorId, "y", movieId));
                
                if(relationExist.hasNext()) {
                    session.close();
                    System.out.println("here3");
                    return 2;
                }
                
            } catch (Exception e) {
                session.close();
                return 3;
            }
            
            // Make the relation ship
            try {
                System.out.println("here2");
                session.writeTransaction(
                        yx -> yx.run("MATCH (a:actor {actorId:$x}), (b:movie {movieId:$y}) \n MERGE (a)-[r:ACTED_IN]->(b)", parameters("x", actorId, "y", movieId)));
                System.out.println("here4");
                session.close();
                return 1;
            } catch (Exception e) {
                System.out.println("Error1");
                session.close();
                return 3;
            }
            
        } catch (Exception e) {
            return 3;
        }
    }
    
    public int getActor(String actorId) {
		try (Session session = driver.session()) {

			try (Transaction tx = session.beginTransaction()) {
				
				Result actorIdResult = tx.run("MATCH (a:actor {actorId: $x}) \n RETURN a.actorId", parameters("x", actorId));
				if (!actorIdResult.hasNext()) {
					session.close();
					return 4;
				}
				
				Result actorResult = tx.run("MATCH (a:actor {actorId: $x}) \n RETURN a.name", parameters("x", actorId));
				
				Result moviesResult = tx.run("MATCH (a:actor {actorId: $x}) - [​r:ACTED_IN] -> (b:movie) \n RETURN b.name", parameters("x", actorId));
				
				session.close();
				return 1;
				
				
			} catch (Exception e) {
				session.close();
				return 3;
			}

		} catch (Exception e) {
			return 3;
		}
	}

    public void close() {
        driver.close();
    }
}
