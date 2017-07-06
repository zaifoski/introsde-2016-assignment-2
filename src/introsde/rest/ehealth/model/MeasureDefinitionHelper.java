package introsde.rest.ehealth.model;

import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import antlr.collections.List;


public class MeasureDefinitionHelper {
	
	private ArrayList measureTypes = new ArrayList();
	
	public void setMeasureTypes(ArrayList<String> measureTypes){
		measureTypes=measureTypes;
	}
	
	public ArrayList<String> getMeasureTypes(){
		return measureTypes;
	}

}
