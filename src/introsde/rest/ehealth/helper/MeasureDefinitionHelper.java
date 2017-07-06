package introsde.rest.ehealth.helper;

import java.io.Serializable;


import javax.xml.bind.annotation.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="measureTypes")
public class MeasureDefinitionHelper implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@XmlElement(name="measureType")
	private List<String> listMeasures = null;

	public List<String> getListMeasures() {
		return listMeasures;
	}

	public void setListMeasures(List<String> listMeasures) {
		this.listMeasures=listMeasures;
	}

}
