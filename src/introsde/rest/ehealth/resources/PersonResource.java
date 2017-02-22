package introsde.rest.ehealth.resources;

import introsde.rest.ehealth.model.HealthMeasureHistory;
import introsde.rest.ehealth.model.LifeStatus;
import introsde.rest.ehealth.model.MeasureDefinition;
import introsde.rest.ehealth.model.Person;

import java.io.IOException;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


@Stateless // only used if the the application is deployed in a Java EE container
@LocalBean // only used if the the application is deployed in a Java EE container
public class PersonResource {
    @Context
    UriInfo uriInfo;
    @Context
    Request request;
    int id;

    EntityManager entityManager; // only used if the application is deployed in a Java EE container

    public PersonResource(UriInfo uriInfo, Request request,int id, EntityManager em) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.id = id;
        this.entityManager = em;
    }

    public PersonResource(UriInfo uriInfo, Request request,int id) {
        this.uriInfo = uriInfo;
        this.request = request;
        this.id = id;
    }
	    
	/*
	 *Request #2: GET /person/{id} should give all the personal information plus current
	 *measures of person identified by {id} (e.g., current measures means current health profile) 
	 */
    // Application integration
    @GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Person getPerson() {
        Person person = this.getPersonById(id);
        if (person == null)
            throw new RuntimeException("Get: Person with " + id + " not found");
        return person;
    }
    // for the browser
    @GET
    @Produces(MediaType.TEXT_XML)
    public Person getPersonHTML() {
        Person person = this.getPersonById(id);
        if (person == null)
            throw new RuntimeException("Get: Person with " + id + " not found");
        System.out.println("Returning person... " + person.getIdPerson());
        return person;
    }

    
    /*
     * Request #3: PUT /person/{id} should update the personal information of the person
     * identified by {id} (e.g., only the person's information, not the measures of the 
     * health profile)
     */
    @PUT
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response putPerson(Person person) {
        System.out.println("--> Updating Person... " +this.id);
        System.out.println("--> "+person.toString());
        Person.updatePerson(person);
        Response res;
        Person existing = getPersonById(this.id);

        if (existing == null) {
            res = Response.noContent().build();
        } else {
            res = Response.created(uriInfo.getAbsolutePath()).build();
            person.setIdPerson(this.id);
            Person.updatePerson(person);
        }
        return res;
    }

    
    /*
     * Request #5: DELETE /person/{id} should delete the person identified by {id} from the system
     */
    @DELETE
    public void deletePerson() {
        Person c = getPersonById(id);
        if (c == null)
            throw new RuntimeException("Delete: Person with " + id
                    + " not found");
        Person.removePerson(c);
    }

    public Person getPersonById(int personId) {
        System.out.println("Reading person from DB with id: "+personId);

        // this will work within a Java EE container, where not DAO will be needed
        //Person person = entityManager.find(Person.class, personId); 

        Person person = Person.getPersonById(personId);
        System.out.println("Person: "+person.toString());
        return person;
    }
    

    /*
     * Request #8: POST /person/{id}/{measureType} should save a new value for the {measureType}
     * (e.g. weight) of person identified by {id} and archive the old value in the history
     */
    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
	@Path("{measureType}")
    public LifeStatus newMeasure(@PathParam("measureType") String measureType, 
			HealthMeasureHistory measureHistory) throws IOException {

		//find a measureDefinition associated with the name of the measure
		MeasureDefinition measureDef = MeasureDefinition.getMeasureDefinition(measureType);
		//find a person identified by a id
		Person person = Person.getPersonById(this.id);
		
		//remove current lifeStatus for a specified person and measureDefinition 
		LifeStatus ls = LifeStatus.getFilteredLifeStatus(measureDef, person);
		if(ls != null){
			LifeStatus.removeLifeStatus(ls);
		}
		
		//save new LifeStatus into db
		LifeStatus newLifeStatus = new LifeStatus(measureDef, measureHistory.getValue(), person);
		newLifeStatus = LifeStatus.saveLifeStatus(newLifeStatus);
		
		//set measureDefinition for measureName
		measureHistory.setMeasureDefinition(measureDef);
		//set person of the new MeasureHistory
		measureHistory.setPerson(person);
		//archive a new measure value in the history and save into db 
		HealthMeasureHistory.saveHealthMeasureHistory(measureHistory);
		
		return LifeStatus.getLifeStatusById(newLifeStatus.getIdMeasure());
		
		/*
        Person p = Person.getPersonById(id);
        List<LifeStatus> listLifeStatus = p.getLifeStatus();
        listLifeStatus.add(lifeStatus);
        Person.updatePerson(p).setLifeStatus(listLifeStatus);
        return p;
        */
    }
}