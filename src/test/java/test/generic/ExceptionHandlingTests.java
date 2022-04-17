package test.generic;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import org.junit.Test;

import helio.Helio;
import helio.blueprints.TranslationUnit;
import helio.blueprints.UnitBuilder;
import helio.blueprints.components.Components;
import helio.blueprints.exceptions.ExtensionNotFoundException;
import helio.blueprints.exceptions.IncompatibleMappingException;
import helio.blueprints.exceptions.IncorrectMappingException;
import helio.blueprints.exceptions.TranslationUnitExecutionException;
import helio.tests.TestUtils;

public class ExceptionHandlingTests {

//	@Test
//	public void test2() throws IncompatibleMappingException, IncorrectMappingException, ExtensionNotFoundException, TranslationUnitExecutionException  {
//		boolean exceptionThrown = false;
//		ExecutorService service = Executors.newCachedThreadPool();
//		Helio helio = new Helio(3);
//		TestUtils.voidMethod();
//		System.out.println(Components.getMappingProcessors());
//		UnitBuilder processor = Components.getMappingProcessors().get("JLD11Builder");
//		
//		try {
//			Set<TranslationUnit> units = processor.parseMapping(mappingContent);
//			TranslationUnit unit = units.iterator().next();
//			Future<Void> f = service.submit(unit.getTask());
//			f.get();
//			//helio.add(unit);
//			//helio.readAndFlush(unit.getId());
//			
//		}catch (Exception e) {
//			e.printStackTrace();
//			exceptionThrown = true;
//		}
//		
//		Assert.assertTrue(exceptionThrown);
//	}
	
	@Test
	public void test1() throws IncompatibleMappingException, IncorrectMappingException, ExtensionNotFoundException, TranslationUnitExecutionException  {
		boolean exceptionThrown = false;
		Helio helio = new Helio();
		Set<TranslationUnit> units  =TestUtils.processJMapping("/Users/andreacimmino/Desktop/Lab/helio-core/src/test/resources/exceptions/mapping-01.json");
		TranslationUnit unit= units.iterator().next();

		try {
			
			unit.getTask().run();
			unit.getDataTranslated();
		}catch (Exception e) {
			exceptionThrown = true;
		}
		
		Assert.assertTrue(exceptionThrown);
	}
	
	private static String mappingContent = "<#assign data2=providers(type=\"FileProvider\", file=\"./src/test/resources/data-samples/sample2.json\")>\n"
			+ "{\n"
			+ "  \"@context\": \"https://www.w3.org/2019/wot/td/v1\",\n"
			+ "  \"id\": \"urn:dev:ops:32473-WoTLamp-\",\n"
			+ "  \"dummy\": [=data2]\n"
			+ "}";
	
	
	
}
