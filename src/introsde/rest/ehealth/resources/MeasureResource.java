package introsde.rest.ehealth.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import introsde.rest.ehealth.helper.MeasureDefinitionHelper;
import introsde.rest.ehealth.model.MeasureDefinition;
import introsde.rest.ehealth.model.Person;

@Stateless // will work only inside a Java EE application
@LocalBean // will work only inside a Java EE application
@Path("/measureTypes")

public class MeasureResource {    
	
	/*
	 *Request #9: GET /measureTypes should return the list of measures your model supports
	 */
	@GET
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public MeasureDefinitionHelper getAllMeasureTypes() {
		List<MeasureDefinition> allMeasureTypes = MeasureDefinition.getAll();
		List measureNames = null;
		for(MeasureDefinition measureType: allMeasureTypes){
			measureNames.add(measureType.getMeasureName());
		}
		MeasureDefinitionHelper mdh = new MeasureDefinitionHelper();
		mdh.setListMeasures(measureNames);
        return mdh;
    }

}
