package introsde.rest.ehealth.test.model;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.glassfish.jersey.client.ClientConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.util.JSONPObject;

import java.io.PrintStream;

@XmlRootElement(name="people")
public class EhealthClient {
	
	static String serverUri = "https://ass2zai.herokuapp.com/sdelab/";
	
	static String content = "";
	
	static HashMap<String, String> firstPerson = new HashMap<String, String>(); 
	
	public static void printResponse(
			String number, String httpMethod,
			String url, String acceptType, String contentType,
			String responseStatus, String httpStatus, String body){
		String printedRes;
		printedRes = "Request #" + number + ": " + httpMethod + " " +  url;
		printedRes += " Accept: [" + acceptType + "] Content-type: " + contentType;
		printedRes += "\n	=> Result: " + responseStatus;
		printedRes += "\n	=> HTTP Status: "+httpStatus;
		printedRes += "\n " + body;
		System.out.println(printedRes);
/*Example: 
Request #7: GET /person/5/weight/899 Accept: APPLICATION/XML Content-Type: APPLICATION/XML  
=> Result: OK
=> HTTP Status: 200
<measure>
    <mid>899</mid>
    <value>72</value>
    <created>2011-12-09</created>
</measure>""
*/
			   
	}
	
	public static Response makeRequest(String path, String mediaType, String method, String input){

		URI server = UriBuilder.fromUri(serverUri).build();
		ClientConfig clientConfig = new ClientConfig();
		Client client = ClientBuilder.newClient(clientConfig);
		WebTarget service = client.target(server);
		
		Response response = null;
		
		if(method=="get")
		response = service.path(path).request(mediaType).accept(mediaType)
				.get(Response.class);
		else if(method=="put")
		response = service.path(path).request(mediaType).accept(mediaType)
				.put(Entity.entity(input, mediaType), Response.class);

		return response;
	}
	
	public static Element getRootElement(Response response) 
			throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = domFactory.newDocumentBuilder();
		content = response.readEntity(String.class);
		Document doc = builder.parse(new InputSource(new StringReader(content)));
		Element rootElement = doc.getDocumentElement();
		return rootElement;
	}

	public static void setLogFile(String name, Boolean append){		
		try {
		    System.setOut(new PrintStream(new FileOutputStream("client-server-"+name+".log",append)));
		} catch (Exception e) {
		     e.printStackTrace();
		}
	}
	
	public static String doRequest31(String path, String mediaType, String result) throws ParserConfigurationException, SAXException, IOException{

		//		Step 3.1. Send R#1 (GET BASE_URL/person).
		//		Calculate how many people are in the response.
		//		If more than 2, result is OK, else is ERROR (less than 3 persons).
		//		Save into a variable id of the first person (first_person_id)
		//		and of the last person (last_person_id)
		
		path = "/person";
		
		Response response = makeRequest(path,mediaType,"get","");
		result = response.getStatusInfo().toString();
		String first_person_id = null;
		String last_person_id = null;
		if(mediaType==MediaType.APPLICATION_XML){
			
			setLogFile("xml",true);
			
			Element rootElement = getRootElement(response);
	
			NodeList listNode = rootElement.getChildNodes();
			
			if(listNode.getLength()<= 2)
				result = "ERROR";
			
			NodeList first_person = rootElement.getFirstChild().getChildNodes();
			for(int i = 0; i < first_person.getLength(); i++){
				firstPerson.put(first_person.item(i).getNodeName(),
						first_person.item(i).getTextContent());
				if (first_person.item(i).getNodeName().equals("idPerson")){
					first_person_id = first_person.item(i).getTextContent();
	
				}
			}
			NodeList last_person = rootElement.getLastChild().getChildNodes();
			for(int i = 0; i < last_person.getLength(); i++){
				if (last_person.item(i).getNodeName().equals("idPerson")){
					last_person_id = last_person.item(i).getTextContent();
	
				}
			}
		}
		else {
			
			setLogFile("json",true);
			
			content = response.readEntity(String.class);
			JSONArray arr = new JSONArray(content);
			if(arr.length() <= 2)
				result = "ERROR";
			first_person_id = arr.getJSONObject(0).get("idPerson").toString();
			last_person_id = arr.getJSONObject(arr.length()-1).get("idPerson").toString();
		}
		String contentType = "";
		if(response!=null && response.getHeaders()!=null && 
				response.getHeaders().get("Content-Type")!=null &&
				response.getHeaders().get("Content-Type").toString()!=null){
			contentType = response.getHeaders().get("Content-Type").toString();
		}
		printResponse("1", "GET", path, mediaType,
				contentType,
				result,
				Integer.toString(response.getStatus()),
				content);
		
		System.out.println(first_person_id);
		System.out.println(last_person_id);
		
		return first_person_id;
	}

	public static void doRequest32(String path, String mediaType, String result,
			String first_person_id) throws ParserConfigurationException, SAXException, IOException{
		
		//	Step 3.2. Send R#2 for first_person_id. If the responses for this is 200 or 202,
		//	the result is OK.
		
		path="/person/"+first_person_id;
		
		Response response = makeRequest(path,mediaType,"get","");
		result = response.getStatusInfo().toString();
		if(mediaType==MediaType.APPLICATION_XML){
			if(response.getStatus()!=200 && response.getStatus()!=202)
				result = "ERROR";
			setLogFile("xml",true);
			if(!result.equals("ERROR"))
				getRootElement(response);
			else content = "";
		}
		else{
			setLogFile("json",true);
			if(response.getStatus()!=200 && response.getStatus()!=202)
				result = "ERROR";
			if(!result.equals("ERROR"))
				content = response.readEntity(String.class);
			else content = "";
		}
		String contentType = "";
		if(response!=null && response.getHeaders()!=null && 
				response.getHeaders().get("Content-Type")!=null &&
				response.getHeaders().get("Content-Type").toString()!=null){
			contentType = response.getHeaders().get("Content-Type").toString();
		}
		printResponse("2", "GET", path, mediaType,
				contentType,
				result,
				Integer.toString(response.getStatus()),
				content);
	}

	public static void doRequest33(String path, String mediaType, String result,
			String first_person_id) throws ParserConfigurationException, SAXException, IOException{
		
		//		Step 3.3. Send R#3 for first_person_id changing the firstname. 
		//		If the responses has the name changed, the result is OK.
		//		R#3 => PUT /person/{id}
		
		path="/person/"+first_person_id;
		String newName = "Sofia";
		
		String input = "";
		Iterator<Entry<String, String>> itr = firstPerson.entrySet().iterator();
		while(itr.hasNext()){
			Entry<String, String> next = itr.next();
			String value = next.getValue();
			if(next.getKey()=="name") value = newName;
			input += "<";
			input += next.getKey();
			input += ">";
			input += value;
			input += "</";
			input += next.getKey();
			input += ">";
			System.out.println(value);			
		}

		Response response = makeRequest(path,mediaType,"put",input);
		result = response.getStatusInfo().toString();

		if(mediaType==MediaType.APPLICATION_XML){
			
			setLogFile("xml",true);
			
			Element rootElement = getRootElement(response);
	
			NodeList listNode = rootElement.getChildNodes();
			
			NodeList children = rootElement.getChildNodes();
			for(int i = 0; i < children.getLength(); i++){
				if (children.item(i).getNodeName().equals("name")){
					if(!children.item(i).getTextContent().equals(newName))
						result = "ERROR";	
				}
			}
		}
		else {
			
			setLogFile("json",true);
			
			content = response.readEntity(String.class);
			JSONObject arr = new JSONObject(content);
			if(arr.getJSONObject("name").toString()==newName)
				result = "ERROR";
		}
		String contentType = "";
		if(response!=null && response.getHeaders()!=null && 
				response.getHeaders().get("Content-Type")!=null &&
				response.getHeaders().get("Content-Type").toString()!=null){
			contentType = response.getHeaders().get("Content-Type").toString();
		}
		printResponse("1", "PUT", path, mediaType,
				contentType,
				result,
				Integer.toString(response.getStatus()),
				content);
		
	}
	
	public static void main(String args[]) throws Exception {
		
		/*
		Response response = makeRequest("/person",MediaType.APPLICATION_XML);
		Element rootElement = getRootElement(response);

		NodeList listNode = rootElement.getChildNodes();
		
		for(int i = 0; i < listNode.getLength(); i++){
			System.out.println(listNode.item(i).getTextContent());
			System.out.println(listNode.item(i).getNodeName());
		}
		String first_person_id = rootElement.getFirstChild().getChildNodes().item(0).getTextContent();
		String last_person_id = rootElement.getLastChild().getChildNodes().item(0).getTextContent();
		
		System.out.println(first_person_id);
		System.out.println(last_person_id);
		*/

		setLogFile("json",false);
		setLogFile("xml",false);
		System.out.println("serverUri: "+serverUri);
		
		String result = "ERROR";		
		String path = "";
		String mediaType = MediaType.APPLICATION_XML;
		doRequest31(path, mediaType, result);
		
		mediaType = MediaType.APPLICATION_JSON;
		String first_person_id = doRequest31(path, mediaType, result);
		
		mediaType = MediaType.APPLICATION_XML;
		doRequest32(path, mediaType, result, first_person_id);
		
		mediaType = MediaType.APPLICATION_JSON;
		doRequest32(path, mediaType, result,  first_person_id);
		
		mediaType = MediaType.APPLICATION_XML;
		doRequest33(path, mediaType, result,  first_person_id);
		
		mediaType = MediaType.APPLICATION_JSON;
		doRequest33(path, mediaType, result,  first_person_id);

//		Step 3.4. Send R#4 to create the following person. Store the id of the new person. If the answer is 201 (200 or 202 are also applicable) with a person in the body who has an ID, the result is OK.
//
//		    {
//		          "firstname"     : "Chuck",
//		          "lastname"      : "Norris",
//		          "birthdate"     : "1945-01-01",
//		          "healthProfile" : {
//		                    "weight"  : 78.9,
//		                    "height"  : 172
//		          }
//		    }
//		Step 3.5. Send R#5 for the person you have just created. Then send R#1 with the id of that person. If the answer is 404, your result must be OK.
//
//		Step 3.6. Follow now with the R#9 (GET BASE_URL/measureTypes). If response contains more than 2 measureTypes - result is OK, else is ERROR (less than 3 measureTypes). Save all measureTypes into array (measure_types)
//		Step 3.7. Send R#6 (GET BASE_URL/person/{id}/{measureType}) for the first person you obtained at the beginning and the last person, and for each measure types from measure_types. If no response has at least one measure - result is ERROR (no data at all) else result is OK. Store one measure_id and one measureType.
//		Step 3.8. Send R#7 (GET BASE_URL/person/{id}/{measureType}/{mid}) for the stored measure_id and measureType. If the response is 200, result is OK, else is ERROR.
//		Step 3.9. Choose a measureType from measure_types and send the request R#6 (GET BASE_URL/person/{first_person_id}/{measureType}) and save count value (e.g. 5 measurements). Then send R#8 (POST BASE_URL/person/{first_person_id}/{measureType}) with the measurement specified below. Follow up with another R#6 as the first to check the new count value. If it is 1 measure more - print OK, else print ERROR. Remember, first with JSON and then with XML as content-types
//
//		        <measure>
//		            <value>72</value>
//		            <created>2011-12-09</created>
//		        </measure>
//		Step 3.10. Send R#10 using the {mid} or the measure created in the previous step and updating the value at will. Follow up with at R#6 to check that the value was updated. If it was, result is OK, else is ERROR.
//
//		      <measure>
//		          <value>90</value>
//		          <created>2011-12-09</created>
//		      </measure>
//		Step 3.11. Send R#11 for a measureType, before and after dates given by your fellow student (who implemented the server). If status is 200 and there is at least one measure in the body, result is OK, else is ERROR
//
//		Step 3.12. Send R#12 using the same parameters as the previous steps. If status is 200 and there is at least one person in the body, result is OK, else is ERROR

	}
}