package introsde.rest.ehealth;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import introsde.rest.ehealth.model.HealthMeasureHistory;
import introsde.rest.ehealth.model.LifeStatus;
import introsde.rest.ehealth.model.Person;

public class UpdatePeople {

	public UpdatePeople(){}
	
	public static void updatePeople(){
	    List<Person> people = Person.getAll();
	    for (int i = 0; i < people.size(); i++){
	    	Person p = people.get(i);
	    	List<LifeStatus> actualMeasures = p.getLifeStatus();
	    	List<LifeStatus> measuresToAdd = new ArrayList<LifeStatus>();
	    	for(int j=0; j< actualMeasures.size();j++){
	    		LifeStatus actualMeasure = actualMeasures.get(j);
				String actualMeasureName = actualMeasure.getMeasureDefinition().getMeasureName();
				//Date actualMeasureDate = actualMeasure.getTimestamp();
				Integer actualMeasurePerson = actualMeasure.getPerson().getIdPerson();
		    	List<HealthMeasureHistory> newMeasures = HealthMeasureHistory.getAll();
    			HealthMeasureHistory mostRecent = null;
    			int measuresUniqueId=1;//
	    		for(int k = 0; k < newMeasures.size(); k++){
	    			HealthMeasureHistory newMeasure = newMeasures.get(k);
	    			String newMeasureName = newMeasure.getMeasureDefinition().getMeasureName();
	    			Date newMeasureDate = newMeasure.getTimestamp();
	    			Integer newMeasurePerson = newMeasure.getPerson().getIdPerson();
	    			newMeasure.setIdMeasureHistory(measuresUniqueId++);//
		    		if(
			    			newMeasurePerson == actualMeasurePerson
		    				&& actualMeasureName.equals(newMeasureName)
		    				//&& actualMeasureDate < newMeasureDate
		    		){
		    			if(
		    					mostRecent == null ||
		    					newMeasureDate.after(mostRecent.getTimestamp())
		    			){
		    				mostRecent = newMeasure;
		    			}
		    		}
	    		}
	    		if (mostRecent != null){
	    			actualMeasure.setValue(mostRecent.getValue());
	    			measuresToAdd.add(actualMeasure);
	    			p.setLifeStatus(measuresToAdd);
	    			Person.updatePerson(p);
	    		}
	    	}
			//p.setLifeStatus(measuresToAdd);
			//Person.updatePerson(p);
	    } 
	}
}
