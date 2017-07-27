package introsde.rest.ehealth.test.model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
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
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import introsde.rest.ehealth.model.Person;

@XmlRootElement(name="people")
public class EhealthClient {
	
	//static String serverUri = "https://ass2zai.herokuapp.com/sdelab/";
	static String serverUri = "http://localhost:5700/sdelab/";
	
	static String content = "";
	
	static HashMap<String, String> firstPerson = new HashMap<String, String>(); 
	
	static String first_person_xml;
	static String first_person_json;
	static String last_person_id = null;

	static String newName = "Sofia";
		
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
	
	public static Response makeRequest(String path, String mediaType, 
			String method, String input) throws ParseException{

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
		else if(method=="post"){
			response = service
					.path(path)
					.request()
					.accept(mediaType)
					.post(Entity.entity(input, mediaType), Response.class);
		}
		else if(method=="delete"){
			response = service
					.path(path)
					.request()
					.accept(mediaType)
					.delete();
		}

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
	
	public static String doRequest31(String path, String mediaType, String result) throws ParserConfigurationException, SAXException, IOException, ParseException{

		//		Step 3.1. Send R#1 (GET BASE_URL/person).
		//		Calculate how many people are in the response.
		//		If more than 2, result is OK, else is ERROR (less than 3 persons).
		//		Save into a variable id of the first person (first_person_id)
		//		and of the last person (last_person_id)
		
		path = "/person";
		
		Response response = makeRequest(path,mediaType,"get","");
		result = response.getStatusInfo().toString();
		String first_person_id = null;
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
			String first_person_id) throws ParserConfigurationException, SAXException, IOException, ParseException{
		
		//	Step 3.2. Send R#2 for first_person_id. If the responses for this is 200 or 202,
		//	the result is OK.
		
		path="/person/"+first_person_id;
		
		Response response = makeRequest(path,mediaType,"get","");

		result = response.getStatusInfo().toString();
		if(mediaType==MediaType.APPLICATION_XML){
			if(response.getStatus()!=200 && response.getStatus()!=202)
				result = "ERROR";
			setLogFile("xml",true);
			if(!result.equals("ERROR")){
				Element r1 = getRootElement(response);				
				//for NEXT request
				for(int i = 0; i < r1.getChildNodes().getLength(); i++){
					if (r1.getChildNodes().item(i).getNodeName().equals("name")){		
						r1.getChildNodes().item(i).setTextContent(newName);
					}
				}
				DOMImplementationLS lsImpl = (DOMImplementationLS)r1.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
				LSSerializer serializer = lsImpl.createLSSerializer();
				serializer.getDomConfig().setParameter("xml-declaration", false); //by default its true, so set it to false to get String without xml-declaration
				first_person_xml = serializer.writeToString(r1);
			}
			else content = "";
		}
		else{
			setLogFile("json",true);
			if(response.getStatus()!=200 && response.getStatus()!=202)
				result = "ERROR";
			if(!result.equals("ERROR")){
				content = response.readEntity(String.class);
			}
			else content = "";

			/*for next request: */
			JSONObject obj = new JSONObject(content);
			obj.put("name",newName);
			first_person_json = obj.toString();

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
			String first_person_id) throws ParserConfigurationException, SAXException, IOException, ParseException{
		
		//		Step 3.3. Send R#3 for first_person_id changing the firstname. 
		//		If the responses has the name changed, the result is OK.
		//		R#3 => PUT /person/{id}
		
		path="/person/"+first_person_id;
		Response response;

		if(mediaType==MediaType.APPLICATION_XML){
			
			setLogFile("xml",true);
			
			/*
			String input = "<person>";
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
			}
			input+= "</person>";
			*/
			
			String input = first_person_xml;
			response = makeRequest(path,mediaType,"put",input);
			result = response.getStatusInfo().toString();
			if(response.getStatus()!=200 && response.getStatus()!=202)
				result = "ERROR";
			if(!result.equals("ERROR")){
				getRootElement(response);
			}
		}
		else {
			
			setLogFile("json",true);

			/*
			String input = "{";
			Iterator<Entry<String, String>> itr = firstPerson.entrySet().iterator();
			while(itr.hasNext()){
				Entry<String, String> next = itr.next();
				String value = next.getValue();
				if(next.getKey()=="name") value = newName;
				input += next.getKey();
				input += ":";
				input += value;
				input += ",";
			}
			input+= "}";
			*/
			String input = first_person_json;
			response = makeRequest(path,mediaType,"put",input);
			result = response.getStatusInfo().toString();
			content = response.readEntity(String.class);
			JSONObject arr = new JSONObject(content);
			if(arr.get("name")==newName)
				result = "ERROR";
		}
		String contentType = "";
		if(response!=null && response.getHeaders()!=null && 
				response.getHeaders().get("Content-Type")!=null &&
				response.getHeaders().get("Content-Type").toString()!=null){
			contentType = response.getHeaders().get("Content-Type").toString();
		}
		printResponse("3", "PUT", path, mediaType,
				contentType,
				result,
				Integer.toString(response.getStatus()),
				content);
		
	}
	
	public static int doRequest34(String path, String mediaType, String result) throws ParserConfigurationException, SAXException, IOException, ParseException{

		//		Step 3.4. Send R#4 to create the following person.
		//		Store the id of the new person. If the answer is 201 (200 or 202 are also applicable) 
		//		with a person in the body who has an ID, the result is OK.
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
		
		path = "/person";
		Response response = null;
		String contentType = "";
		int new_person_id = -1;

		if(mediaType==MediaType.APPLICATION_XML){
			setLogFile("xml",true);
			
			String input = "<person>" 
					+ "<name>Mario</name>"
					+ "<lastname>Rossi</lastname>"
					+ "<birthdate>01/01/1945</birthdate>"
					+ "<username>marrossi</username>"
					+ "<email>mario@rossi.com</email>"
					+ "</person>";

			response = makeRequest(path,mediaType,"post",input);
			result = response.getStatusInfo().toString();
			Element rootElement = getRootElement(response);
	
			NodeList listNode = rootElement.getChildNodes();
			for(int i = 0; i < listNode.getLength(); i++){
				if(listNode.item(i).getNodeName().equals("idPerson")){
					new_person_id = Integer.parseInt(listNode.item(i).getTextContent());
				}
			}
			if(response.getStatus()!=200 && response.getStatus()!=202)
				result = "ERROR";
		}
		else {
			
			setLogFile("json",true);
			
			JSONObject obj = new JSONObject();
			obj.put("name", "Mario");
			obj.put("lastname", "Rossi");
			obj.put("birthdate", "01/09/1978");	
			obj.put("username", "marrossi");	
			obj.put("email", "mario@rossi.com");			
			String input = obj.toString();
			response = makeRequest(path,mediaType,"post",input);
			result = response.getStatusInfo().toString();
			if(response.getStatus()!=200 && response.getStatus()!=202)
				result = "ERROR";
			if(!result.equals("ERROR")){
				content = response.readEntity(String.class);
				JSONObject newobj = new JSONObject(content);
				new_person_id = (int) newobj.get("idPerson");
			}
			else content = "";
		}
		if(response!=null && response.getHeaders()!=null && 
				response.getHeaders().get("Content-Type")!=null &&
				response.getHeaders().get("Content-Type").toString()!=null){
			contentType = response.getHeaders().get("Content-Type").toString();
		}
		printResponse("4", "POST", path, mediaType,
				contentType,
				result,
				Integer.toString(response.getStatus()),
				content);
		return new_person_id;
	}
	
	public static void doRequest35(String path, String mediaType, String result, int id) throws ParseException{
		
		//		Step 3.5. Send R#5 for the person you have just created. 
		//		Then send R#1 with the id of that person. 
		//		If the answer is 404, your result must be OK.
		
		path="/person/"+id;
		Response response = null;
		String contentType = "";
		
		makeRequest(path,mediaType,"delete","");
		response = makeRequest(path, mediaType, "get","");

		if(mediaType==MediaType.APPLICATION_XML){
			setLogFile("xml",true);}
		else {
			setLogFile("json",true);
		}
		if(response.getStatus()==200 || response.getStatus()==202)
			result = "ERROR";
		else result = "OK";
		if(response!=null && response.getHeaders()!=null && 
				response.getHeaders().get("Content-Type")!=null &&
				response.getHeaders().get("Content-Type").toString()!=null){
			contentType = response.getHeaders().get("Content-Type").toString();
		}
		printResponse("5", "DELETE", path, mediaType,
				contentType,
				result,
				Integer.toString(response.getStatus()),
				"");
		
	}

	public static List<String> doRequest36(String path, String mediaType, String result) throws ParserConfigurationException, SAXException, IOException, ParseException{

		//		Step 3.6. Follow now with the R#9 (GET BASE_URL/measureTypes).
		//		If response contains more than 2 measureTypes - result is OK, 
		//		else is ERROR (less than 3 measureTypes). 
		//		Save all measureTypes into array (measure_types)
		
		path = "/measureTypes";
		List<String> measureTypes = new ArrayList<String>(); 
		
		Response response = makeRequest(path,mediaType,"get","");
		result = response.getStatusInfo().toString();
		if(mediaType==MediaType.APPLICATION_XML){
			
			setLogFile("xml",true);
			
			Element rootElement = getRootElement(response);
	
			NodeList listNode = rootElement.getChildNodes();
			
			if(listNode.getLength()<= 2)
				result = "ERROR";
			
			for(int i = 0; i < listNode.getLength(); i++){
				measureTypes.add(listNode.item(i).getTextContent());
			}
		}
		else {
			
			setLogFile("json",true);
			
			content = response.readEntity(String.class);
			JSONObject obj = new JSONObject(content);
			JSONArray arr = obj.getJSONArray("measureType");
			if(arr.length() <= 2)
				result = "ERROR";
			for(int i = 0; i < arr.length(); i++){
				measureTypes.add((String) arr.get(i));
			}
		}
		String contentType = "";
		if(response!=null && response.getHeaders()!=null && 
				response.getHeaders().get("Content-Type")!=null &&
				response.getHeaders().get("Content-Type").toString()!=null){
			contentType = response.getHeaders().get("Content-Type").toString();
		}
		printResponse("6", "GET", path, mediaType,
				contentType,
				result,
				Integer.toString(response.getStatus()),
				content);
		
		return measureTypes;
	}
	
	public static int measure_id = -1;
	public static String measure_type = "";
	
	public static String doRequest37(String path, String mediaType, String result) throws ParseException, ParserConfigurationException, SAXException, IOException {
		
		//		Step 3.7. Send R#6 (GET BASE_URL/person/{id}/{measureType}) 
		//		for the first person you obtained at the beginning and the last person, 
		//		and for each measure types from measure_types. 
		//		If no response has at least one measure - result is ERROR 
		//		(no data at all) else result is OK. 
		//		Store one measure_id and one measureType.
		List<String> measureTypes = new ArrayList<String>(); 
		
		Response response = makeRequest(path,mediaType,"get","");
		result = response.getStatusInfo().toString();
		if(mediaType==MediaType.APPLICATION_XML){
			
			setLogFile("xml",true);
			
			Element rootElement = getRootElement(response);
	
			NodeList listNode = rootElement.getChildNodes();
			
			if(listNode.getLength()< 1)
				result = "ERROR";
			
			if(measure_id==-1 || !measure_type.equals("")){
				for(int i = 0; i < listNode.getLength() && (measure_id==-1 || !measure_type.equals("")); i++){
					NodeList list = listNode.item(i).getChildNodes();
					for(int j = 0; j < list.getLength(); j++){
						if(list.item(j).getNodeName().equals("idMeasureHistory")){
							measure_id = Integer.parseInt(list.item(j).getTextContent());
						}
						if(list.item(j).getNodeName().equals("measureDefinition")){
							NodeList list2 = list.item(j).getChildNodes();
							for(int k = 0; k < list2.getLength(); k++){
								if(list2.item(k).getNodeName().equals("measureName")){
									measure_type = list2.item(k).getTextContent();
								}
							}
						}
					}
				}
			}
		}
		else {
			
			setLogFile("json",true);
			
			content = response.readEntity(String.class);
			try{
				JSONArray arr = new JSONArray(content);
				if(arr.length() < 1)
					result = "ERROR";
				
				if(measure_id==-1 || !measure_type.equals("")){
					for(int i = 0; i < arr.length() && (measure_id==-1 || !measure_type.equals("")); i++){
						JSONObject obj = new JSONObject(arr.get(i));
						try{
							if(obj.get("idMeasureHistory")!=null){
								measure_id = Integer.parseInt((String) obj.get("idMeasureHistory"));
							}
							if(obj.get("measureDefinition")!=null){
								JSONObject obj2 = new JSONObject(obj.get("measureDefinition"));
								if(obj2.get("measureName")!=null){
									measure_type = (String) obj2.get("measureName");
								}
							}
						}catch(Exception e){}
					}
				}
			} catch(Exception e){result = "ERROR";}
		}
		String contentType = "";
		if(response!=null && response.getHeaders()!=null && 
				response.getHeaders().get("Content-Type")!=null &&
				response.getHeaders().get("Content-Type").toString()!=null){
			contentType = response.getHeaders().get("Content-Type").toString();
		}
		printResponse("7", "GET", path, mediaType,
				contentType,
				result,
				Integer.toString(response.getStatus()),
				content);
		return result;
	}
	
	public static void doRequest38(String path, String mediaType, String result) throws ParseException, ParserConfigurationException, SAXException, IOException{
				
		//		Step 3.8. Send R#7 (GET BASE_URL/person/{id}/{measureType}/{mid})
		//		for the stored measure_id and measureType. If the response is 200, 
		//		result is OK, else is ERROR.
		
		Response response = makeRequest(path,mediaType,"get","");
		result = response.getStatusInfo().toString();

		if(response.getStatus()==200 || response.getStatus()==202)
			result = "OK";
		else result = "ERROR";
		
		if(mediaType==MediaType.APPLICATION_XML){
			
			setLogFile("xml",true);
			
			getRootElement(response) ;
		}
		else {
			
			setLogFile("json",true);
			
			content = response.readEntity(String.class);
		}
		String contentType = "";
		if(response!=null && response.getHeaders()!=null && 
				response.getHeaders().get("Content-Type")!=null &&
				response.getHeaders().get("Content-Type").toString()!=null){
			contentType = response.getHeaders().get("Content-Type").toString();
		}
		printResponse("8", "GET", path, mediaType,
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

		mediaType = MediaType.APPLICATION_JSON;
		int id = doRequest34(path, mediaType, result);

		mediaType = MediaType.APPLICATION_JSON;
		doRequest35(path, mediaType, result, id);

		mediaType = MediaType.APPLICATION_XML;
		id = doRequest34(path, mediaType, result);

		mediaType = MediaType.APPLICATION_XML;
		doRequest35(path, mediaType, result, id);
		
		mediaType = MediaType.APPLICATION_XML;
		List<String> measureTypes = doRequest36(path, mediaType, result);
		result = "ERROR"; String finalResult = "ERROR";
		for(String measureType:measureTypes){
			path = "/person/"+first_person_id+"/"+measureType;
			result = doRequest37(path, mediaType, result);
			if (!result.equals("ERROR")) finalResult = "OK";
			path = "/person/"+last_person_id+"/"+measureType;
			result = doRequest37(path, mediaType, result);
			if (!result.equals("ERROR")) finalResult = "OK";
		}
		path = "";
		System.out.println("Request #7: \n\tFINAL RESULT: "+finalResult+"\n\t measure_id: "+measure_id+"\n\t measure_type: "+measure_type);
		result = "ERROR";
		
		mediaType = MediaType.APPLICATION_JSON;
		measureTypes = doRequest36(path, mediaType, result);
		result = "ERROR"; finalResult = "ERROR";
		for(String measureType:measureTypes){
			System.out.println(measureType);
			path = "/person/"+first_person_id+"/"+measureType;
			result = doRequest37(path, mediaType, result);
			if (!result.equals("ERROR")) finalResult = "OK";
			path = "/person/"+last_person_id+"/"+measureType;
			result = doRequest37(path, mediaType, result);
			if (!result.equals("ERROR")) finalResult = "OK";
		}
		path = "";
		System.out.println("Request #7: \n\tFINAL RESULT: "+finalResult+"\n\t measure_id: "+measure_id+"\n\t measure_type: "+measure_type);
		result = "ERROR";

		path = "/person/"+1+"/"+measure_type+"/"+measure_id;
		mediaType = MediaType.APPLICATION_XML;
		doRequest38(path, mediaType, result);
		mediaType = MediaType.APPLICATION_JSON;
		doRequest38(path, mediaType, result);

		
//		Step 3.9. Choose a measureType from measure_types and send
//		the request R#6 (GET BASE_URL/person/{first_person_id}/{measureType}) 
//		and save count value (e.g. 5 measurements).
//		Then send R#8 (POST BASE_URL/person/{first_person_id}/{measureType}) 
//		with the measurement specified below. 
//		Follow up with another R#6 as the first to check the new count value.
//		If it is 1 measure more - print OK, else print ERROR. 
//
//		        <measure>
//		            <value>72</value>
//		            <created>2011-12-09</created>
//		        </measure>

	}
}