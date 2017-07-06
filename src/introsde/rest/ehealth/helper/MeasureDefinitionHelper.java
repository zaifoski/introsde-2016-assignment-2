package helper;

import java.io.Serializable;

import javax.xml.bind.annotation.*;

import java.util.List;

@XmlRootElement(name="measureTypes")
public class MeasureDefinitionHelper implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@XmlElement(name="measureType")
	private List listMeasures = null;

	public List getListMeasures() {
		return listMeasures;
	}

	public void setListMeasures(List listMeasures) {
		this.listMeasures=listMeasures;
	}

}
