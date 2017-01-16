package introsde.rest.ehealth.resources;
import introsde.rest.ehealth.model.HealthMeasureHistory;
import introsde.rest.ehealth.model.Person;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.PersistenceUnit;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

@Stateless // will work only inside a Java EE application
@LocalBean // will work only inside a Java EE application
@Path("/person")
public class PersonCollectionResource {

    // Allows to insert contextual objects into the class,
    // e.g. ServletContext, Request, Response, UriInfo
    @Context
    UriInfo uriInfo;
    @Context
    Request request;

    // will work only inside a Java EE application
    @PersistenceUnit(unitName="introsde-jpa")
    EntityManager entityManager;

    // will work only inside a Java EE application
    @PersistenceContext(unitName = "introsde-jpa",type=PersistenceContextType.TRANSACTION)
    private EntityManagerFactory entityManagerFactory;

    /*
     * Request #1: GET /person should list all the people (see above Person model to know what
     * data to return here) in your database (wrapped under the root element "people")
     */
    // Return the list of people to the user in the browser
    @GET
    @Produces({MediaType.TEXT_XML,  MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML })
    public List<Person> getPersonsBrowser() {
        System.out.println("Getting list of people...");
        List<Person> people = Person.getAll();
        return people;
    }

    // retuns the number of people
    // to get the total number of records
    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String getCount() {
        System.out.println("Getting count...");
        List<Person> people = Person.getAll();
        int count = people.size();
        return String.valueOf(count);
    }
    
    /*
     * Request #4: POST /person should create a new person and return the newly
     * created person with its assigned id (if a health profile is included,
     * create also those measurements for the new person).
     */
    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    public Person newPerson(Person person) throws IOException {
        System.out.println("Creating new person...");          
        return Person.savePerson(person);
    }

    // Defines that the next path parameter after the base url is
    // treated as a parameter and passed to the PersonResources
    // Allows to type http://localhost:599/base_url/1
    // 1 will be treaded as parameter todo and passed to PersonResource
    @Path("{personId}")
    public PersonResource getPerson(@PathParam("personId") int id) {
        return new PersonResource(uriInfo, request, id);
    }
    

    /*
     * Request #6: GET /person/{id}/{measureType} should return the list of values (the history)
     * of {measureType} (e.g. weight) for person identified by {id}
     */
    @GET
    @Path("{id}/{measuretype}")
    @Produces({MediaType.TEXT_XML,  MediaType.APPLICATION_JSON ,  MediaType.APPLICATION_XML })
    public List<HealthMeasureHistory> getMeasureFromPersonId(@PathParam("id") int id,
    	@PathParam("measuretype") String type) {
	    List<HealthMeasureHistory> listAll = HealthMeasureHistory.getAll();
	    List<HealthMeasureHistory> listFiltered = HealthMeasureHistory.getAll();
	    for(int i = 0; i < listAll.size(); i++){
	    	HealthMeasureHistory measure = listAll.get(i);
	    	if (
	    			measure.getMeasureDefinition().getMeasureName().equals(type) &&
	    			measure.getPerson().getIdPerson() == id
	    	){
	    		listFiltered.add(measure);
	    	}
	    }
	    return listFiltered;
    	/*
	    String s = "";
	    if (history.getMeasureDefinition().getMeasureName().equals(type)){
	    	s += "history.getClass() ";
	    	s += history.getClass().toString();
	    	s += "\nhistory.getIdMeasureHistory() :";
	    	s += history.getIdMeasureHistory();
	    	s += "\nhistory.getMeasureDefinition() :";
	    	s += history.getMeasureDefinition().toString();
	    	s += "\nhistory.getPerson() :";
	    	s += history.getPerson().toString();
	    	s += "\nhistory.getTimestamp() .";
	    	s += history.getTimestamp();
	    	s += "\nhistory.getValue() :";
	    	s += history.getValue().toString();
	    	s += "\nhistory.getAll() :";
	    	s += history.getAll().toString();
	    	s += "\nhistory.getHealthMeasureHistoryById(Integer.parseInt(history.getValue())): ";
	    	s += history.getHealthMeasureHistoryById(Integer.parseInt(history.getValue()));
	    }
	    return s;*/
    }
}