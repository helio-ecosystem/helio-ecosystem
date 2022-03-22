package helio.tests;

import org.apache.jena.sparql.resultset.ResultsFormat;
import org.junit.Test;

import helio.Mappings;
import helio.blueprints.exceptions.ExtensionNotFoundException;
import helio.blueprints.exceptions.IncompatibleMappingException;
import helio.blueprints.exceptions.IncorrectMappingException;
import helio.blueprints.mappings.Mapping;
import helio.components.readers.JsonLD11Reader;
import helio.exceptions.ConfigurationException;
import sparql.streamline.exception.SparqlConfigurationException;
import sparql.streamline.exception.SparqlQuerySyntaxException;
import sparql.streamline.exception.SparqlRemoteEndpointException;

public class TestJsonld11 {

	public static final String json = "{\n"
			+ "    \"@context\": \"https://www.w3.org/2019/wot/td/v1\",\n"
			+ "	   \"@datasource\" : {"
			+ "				\"handler\": {\n"
			+ "                \"type\": \"JsonHandler\",\n"
			+ "                \"iterator\": \"$\"\n"
			+ "            },\n"
			+ "            \"provider\": {\n"
			+ "                \"type\": \"URLProvider\",\n"
			+ "                \"url\": \"http://api.icndb.com/jokes/random?firstName=John&lastName=Doe\"\n"
			+ "            }},\n"
			+ "    \"id\": \"urn:dev:ops:32473-∂$.value.idß\",\n"
			+ "    \"title\": \"∂$.typeß\",\n"
			+ "    \"securityDefinitions\": {\n"
			+ "        \"basic_sc\": {\"scheme\": \"basic2\", \"in\":\"header\"}\n"
			+ "    },\n"
			+ "    \"security\": [\"∂$.value.categories.[*]ß\"],\n"
			+ "    \"properties\": {\n"
			+ "        \"∂$.value.jokeß\" : {\n"
			+ "            \"type\": \"string\",\n"
			+ "            \"forms\": [{\"href\": \"https://mylamp.example.com/status\"}]\n"
			+ "        }\n"
			+ "    },\n"
			+ "    \"actions\": {\n"
			+ "        \"toggle\" : {\n"
			+ "            \"forms\": [{\"href\": \"https://mylamp.example.com/toggle\"}]\n"
			+ "        }\n"
			+ "    },\n"
			+ "    \"events\":{\n"
			+ "        \"overheating\":{\n"
			+ "            \"data\": {\"type\": \"string\"},\n"
			+ "            \"forms\": [{\n"
			+ "                \"href\": \"https://mylamp.example.com/oh$.name\",\n"
			+ "                \"subprotocol\": \"longpoll\"\n"
			+ "            }]\n"
			+ "        }\n"
			+ "    }\n"
			+ "}";
	
	
	@Test
	public void test() throws IncompatibleMappingException, IncorrectMappingException, ExtensionNotFoundException, SparqlConfigurationException, ConfigurationException, SparqlQuerySyntaxException, SparqlRemoteEndpointException {
		System.out.println(json);
		long start1 = System.currentTimeMillis();
		JsonLD11Reader reader = new JsonLD11Reader();
		Mapping map = reader.readMapping(json);
		
		long end1 = System.currentTimeMillis();  
	    System.out.println(map.toJson());
		System.out.println("Parsing in m seconds: "+ (end1-start1));   
	    start1 = System.currentTimeMillis();
		TestUtils.helio.addMapping(map);
		end1 = System.currentTimeMillis();  
	    System.out.println("Loading in m seconds: "+ (end1-start1));  
		
	    start1 = System.currentTimeMillis();
	    TestUtils.helio.runSynchronous();
		
		end1 = System.currentTimeMillis();  
	    System.out.println("Generation in m seconds: "+ (end1-start1)); 
		//System.out.println(TestUtils.helio.getRDF(ResultsFormat.FMT_RDF_TURTLE));
	}
	
	

}
