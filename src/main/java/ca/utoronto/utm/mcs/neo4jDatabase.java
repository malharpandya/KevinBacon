package ca.utoronto.utm.mcs;

import static org.neo4j.driver.Values.parameters;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.Value;
import org.json.*;
import java.util.*;

public class neo4jDatabase {
    private Driver driver;
    private String uriDb; 
    private JSONObject response;

    public neo4jDatabase() {
        uriDb = "bolt://localhost:7687";
        driver = GraphDatabase.driver(uriDb, AuthTokens.basic("neo4j", "1234"));
    }
    
    //CLOSE TX
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
                        tx.run("MATCH (a:actor {id:$x}) \n RETURN a.id", parameters("x", actorId));
                if (actorIdResult.hasNext()) {
                    session.close();
                    return 2;
                }

            } catch (Exception e) {
                return 3;
            }

            try {
                session.writeTransaction(
                        tx -> tx.run("MERGE (a:actor {name: $x , id: $y})", parameters("x", actor, "y", actorId)));
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
                        tx.run("MATCH (a:movie {id:$x}) \n RETURN a.id", parameters("x", movieId));
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
                        yx -> yx.run("MERGE (a:movie {name: $x , id: $y})", parameters("x", movie, "y", movieId)));
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
                Result actorExist = tx.run("MATCH (a:actor {id:$x}) \n RETURN a.id", parameters("x", actorId));
                Result movieExist = tx.run("MATCH (a:movie {id:$x}) \n RETURN a.id", parameters("x", movieId));
                
                if(!(actorExist.hasNext() && movieExist.hasNext())) {
                    System.out.println("here1");
                    session.close();
                    return 4;
                }

                // Actor and Movie already have a relation

                Result relationExist = tx.run("MATCH (a:actor {id:$x}), (b:movie {id:$y}) \n MATCH (a)-[r:ACTED_IN]->(b) \n RETURN r", parameters("x", actorId, "y", movieId));
                
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
                        yx -> yx.run("MATCH (a:actor {id:$x}), (b:movie {id:$y}) \n MERGE (a)-[r:ACTED_IN]->(b)", parameters("x", actorId, "y", movieId)));
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
				
			    //Actor does not exist error 404 
				Result actorIdResult = tx.run("MATCH (a:actor {id: $x}) \n RETURN a.id", parameters("x", actorId));
				if (!actorIdResult.hasNext()) {
					session.close();
					return 4;
				}
				
				//Get Actor's name 
				Result actorResult = tx.run("MATCH (a:actor {id: $x}) \n RETURN a.name", parameters("x", actorId));
				String actorString = actorResult.single().get(0).toString();
				actorString = actorString.substring(1, actorString.length() - 1);
                
				Result moviesResult = tx.run("MATCH (a:actor {id:$x}), (b:movie) \n MATCH (a)-[r:ACTED_IN]->(b) \n RETURN b.id", parameters("x", actorId));	
				
				List<String> movieList = new ArrayList<>();
                int i = 0;
                while(moviesResult.hasNext()) {
                    String temp = moviesResult.next().get(i).toString();
                    temp = temp.substring(1, temp.length() - 1);
                    movieList.add(temp);
                    System.out.println(temp);
                }

				response = new JSONObject().put("actorId", actorId).put("name", actorString).put("movies", movieList);
				session.close();
				return 1;
				
				
			} catch (Exception e) {
			    System.out.println("Error1");
				session.close();
				return 3;
			}

		} catch (Exception e) {
		    System.out.println("Error2");
			return 3;
		}
	}
    
    public int getMovie(String movieId) {
        
        try (Session session = driver.session()) {

            try (Transaction tx = session.beginTransaction()) {
                
                System.out.println("1");
                //Movie does not exist error 404 
                Result movieIdResult = tx.run("MATCH (a:movie {id: $x}) \n RETURN a.id", parameters("x", movieId));
                if (!movieIdResult.hasNext()) {
                    session.close();
                    return 4;
                }
                
                //Get Movie's name 
                Result movieResult = tx.run("MATCH (a:movie {id: $x}) \n RETURN a.name", parameters("x", movieId));
                String movieString = movieResult.single().get(0).toString();
                movieString = movieString.substring(1, movieString.length() - 1);
                
                Result actorsResult = tx.run("MATCH (a:actor), (b:movie  {id:$x}) \n MATCH (a)-[r:ACTED_IN]->(b) \n RETURN a.id", parameters("x", movieId));   
                
                List<String> actorsList = new ArrayList<>();
                while(actorsResult.hasNext()) {
                    String temp = actorsResult.next().get(0).toString();
                    temp = temp.substring(1, temp.length() - 1);
                    actorsList.add(temp);
                    System.out.println(temp);
                }

                response = new JSONObject().put("movieId", movieId).put("name", movieString).put("actors", actorsList);
                session.close();
                return 1;
                
                
            } catch (Exception e) {
                System.out.println("Error1");
                session.close();
                return 3;
            }

        } catch (Exception e) {
            System.out.println("Error2");
            return 3;
        }
    }
    
    public int hasRelationship(String actorId, String movieId) {
    	try (Session session = driver.session()) {
    		
    		try (Transaction tx = session.beginTransaction()) {
    			
    			Result actorIdResult = tx.run("MATCH (a:actor {id: $x}) \n RETURN a.id", parameters("x", actorId));
    			Result movieIdResult = tx.run("MATCH (a:movie {id: $x}) \n RETURN a.id", parameters("x", movieId));
    			if (!(movieIdResult.hasNext() && actorIdResult.hasNext())) {
                    session.close();
                    return 4;
                }
    			
    			Result hasRelationResult = tx.run("MATCH (a:actor {id:$x}), (b:movie {id:$y}) \n RETURN EXISTS((a)-[:ACTED_IN]->(b))", parameters("x", actorId, "y", movieId));
    			
    			Boolean hasRelationshipbool = hasRelationResult.next().get(0).isTrue();
    			System.out.println(hasRelationshipbool.toString());
    			
    			response = new JSONObject().put("actorId", actorId).put("movieId", movieId).put("hasRelationship", hasRelationshipbool);
                session.close();
                return 1;
    			
    			
    		} catch (Exception e) {
    			return 3;
    		}
    		
    	} catch(Exception e) {
    		return 3;
    	}
    		
    }
    
    public int getBaconNumber(String actorId) {
        
        try (Session session = driver.session()) {
            
            try (Transaction tx = session.beginTransaction()) {
                
                if(actorId.equals("nm0000102")) {
                    session.close();
                    response = new JSONObject().put("baconNumber", "0" );
                    return 1;
                }
                //check if actor exist in database
                Result actorExist = tx.run("MATCH (a:actor {id: $x}) \n RETURN a.id", parameters("x", actorId));
                if(!actorExist.hasNext()) {
       
                    session.close();
                    return 2;
                }
                
                Result checkPath = tx.run("MATCH (a:actor { id: $x }),(b:actor { id: $y }), p = shortestPath((a)-[*..]-(b)) \n RETURN p", parameters("x", actorId , "y", "nm0000102"));
                if(!checkPath.hasNext()) {
                    System.out.println("NOPE");
                    session.close();
                    return 4;
                }
                String num = String.valueOf((checkPath.next().get("p").size())/2);
                
                response = new JSONObject().put("baconNumber", num );
                return 1;
                
                
            } catch (Exception e) {
                return 3;
            }
            
        } catch(Exception e) {
            return 3;
        }
      
    }
    
    public int getBaconPath(String actorId) {
    	try (Session session = driver.session()){
    		
    		try (Transaction tx = session.beginTransaction()){
    			
    		    System.out.println("here1");
    		    int checkPath = this.getBaconNumber(actorId);
    		    List<JSONObject> baconPath = new ArrayList<>();
    		    //IF KEVIN BACON
    		    if(checkPath == 1 && actorId.equals("nm0000102")) {
    		        
    		      Result randomMovieResult = tx.run("MATCH (a:actor {id:$x}), (b:movie) \n MATCH (a)-[r:ACTED_IN]->(b) \n RETURN b.id , rand() as r \n ORDER BY r", parameters("x", "nm0000102"));
    		      if(!randomMovieResult.hasNext()) {
    		        session.close();
    		        return 2;
    		      }
    		      String movieString = randomMovieResult.next().get(0).asString();
    		      
    		      JSONObject randomMovie = new JSONObject().put("actorId", "nm0000102").put("movieId", movieString);
    		      baconPath.add(randomMovie);
    		      response.put("baconPath", baconPath);
    		      session.close();
    		      return 1;
    		    }
    		    else if (checkPath == 1){

    		      Result pathWay = tx.run("MATCH (a:actor { id: $x }),(b:actor { id: $y }), p = shortestPath((a)-[*..]-(b)) \n RETURN [node in nodes(p) | node.id] as nodes", parameters("x", actorId , "y", "nm0000102"));
    		      Value pathWayID = pathWay.next().get("nodes");
    		      
    		      
    		      for(int i = 1; i < pathWayID.size() ; i+=2) {
    		          baconPath.add(new JSONObject().put("actorId", pathWayID.get(i-1).asString()).put("movieId", pathWayID.get(i).asString()));
    		          baconPath.add(new JSONObject().put("actorId", pathWayID.get(i+1).asString()).put("movieId", pathWayID.get(i).asString()));
    		          
    		      }
    		      
    		    
                  response.put("baconPath", baconPath);
                  session.close();
                  return 1;
    		    }
    		    else{
    		        session.close();
    		        return checkPath;
    		    }
    			
    		} catch(Exception e) {
    			return 3;
    		}
    		
    	} catch (Exception e) {
    		return 3;
    	}
    }
    
    public JSONObject getResponse() {
		return this.response;
	}

	public void close() {
        driver.close();
    }
}

//if(unique) {
//
//// checks if actor exists
//Result actorIdResult = tx.run("MATCH(a:actor {id: $x}) \n RETURN a.id", parameters("x", actorId));
//if(!actorIdResult.hasNext()) {
//  session.close();
//  return 2;
//}
//
//Result baconPathResult = tx.run("MATCH (a:actor { id: $x }),(b:actor { id: $y }), p = shortestPath((a)-[*..]-(b)) \n RETURN p", parameters("x", actorId , "y", "nm0000102"));
//if(!baconPathResult.hasNext()) {
//    session.close();
//    return 4; //path not found
//}
//
//String num = String.valueOf((baconPathResult.next().get("p").size())/2);
//
//List<JSONObject> baconPath = new ArrayList<>();
//
//while(baconPathResult.hasNext()) {
//  
//  String id = baconPathResult.next().get("id").asString();
//    System.out.println(id);
//}
//
//
//
//response = new JSONObject().put("baconNumber", num ).put("baconPath", baconPath);
//session.close();
//return 1;
//}
//else {
//// find a random movie of kevin bacon and put in response

//