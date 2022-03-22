package helio.components.readers;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.jena.datatypes.BaseDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.RdfLiteral;
import com.apicatalog.rdf.RdfNQuad;
import com.apicatalog.rdf.RdfValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import helio.Utils;
import helio.blueprints.components.MappingReader;
import helio.blueprints.exceptions.ExtensionNotFoundException;
import helio.blueprints.exceptions.IncompatibleMappingException;
import helio.blueprints.exceptions.IncorrectMappingException;
import helio.blueprints.mappings.Datasource;
import helio.blueprints.mappings.Mapping;
import helio.blueprints.mappings.TranslationRule;
import helio.blueprints.mappings.TranslationRules;

public class JsonLD11Reader implements MappingReader {

	private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	private static JsonLdOptions options = new JsonLdOptions();
	
	public Mapping readMapping(String content) throws IncompatibleMappingException, IncorrectMappingException, ExtensionNotFoundException {
		JsonObject mappingJson = null;
		try {
			mappingJson = GSON.fromJson(content, JsonObject.class);
		}catch(Exception e) {
			throw new IncompatibleMappingException(e.toString());
		}
		if(mappingJson!=null) {
			Mapping mapping = new Mapping();
			Datasource datasource = initiateDatasource(mappingJson);
			mapping.getDatasources().add(datasource);

			mappingJson.remove("@datasource");
			List<TranslationRules> rules = instantiateResourceRules(mappingJson.toString(), datasource.getId() );
			mapping.getTranslationRules().addAll(rules);
			
			return mapping;
		}
		
		return null;
	}

	private Datasource initiateDatasource(JsonObject mappingJson) throws IncorrectMappingException {
		if(!mappingJson.has("@datasource"))
			throw new IncorrectMappingException("Mapping must have a data source defined under key 'datasource'");
		JsonObject object = mappingJson.remove("@datasource").getAsJsonObject();
		if(!object.has("id"))
			object.addProperty("id", UUID.randomUUID().toString());
		
		return Utils.GSON.fromJson(object, Datasource.class);
	}
	
	private List<TranslationRules> instantiateResourceRules(String mappingJson, String datasourceId) {
		List<TranslationRules> trs = new ArrayList<>();
		try {
			// translate into NT and fill a jena model
			Document document = JsonDocument.of(new ByteArrayInputStream(mappingJson.toString().getBytes()));
			RdfDataset triples = JsonLd.toRdf(document).options(options).get();
			Model model = ModelFactory.createDefaultModel();
			triples.toList().forEach(t -> model.add(toTriple(t)));
			// Transform all statements with the same subject into mapping
			model.listSubjects().toList()
				.stream()
				.map(subject -> model.listStatements(subject, null, (RDFNode) null).toList())
				.map(triplets -> toTranslationRules(triplets, datasourceId))
				.forEach(tr -> trs.add(tr));
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return trs;
	}

	private static Model toTriple(RdfNQuad quadTriple) {
		Model model = ModelFactory.createDefaultModel();
		
		try {
		Resource subject = ResourceFactory.createResource(quadTriple.getSubject().toString());
		Property predicate =  ResourceFactory.createProperty(quadTriple.getPredicate().toString());
		RdfValue objectRaw = quadTriple.getObject();
		
		if(objectRaw.isIRI() || objectRaw.isBlankNode()) {
			Resource object =  ResourceFactory.createResource(quadTriple.getObject().getValue());
			model.add(subject, predicate, (RDFNode) object);
		}else {
			RdfLiteral literal = objectRaw.asLiteral();
			Literal jenaLiteral = ResourceFactory.createPlainLiteral(literal.getValue());
			if(literal.getLanguage().isPresent()) {
				jenaLiteral = ResourceFactory.createLangLiteral(literal.getValue(), literal.getLanguage().get());
			}else if(literal.getDatatype()!=null && !literal.getDatatype().isEmpty()) {
				jenaLiteral = ResourceFactory.createTypedLiteral(literal.getValue(), new BaseDatatype(literal.getDatatype()));
			}
			model.add(subject, predicate, jenaLiteral);
		}
		}catch(Exception e) {
			System.out.println("Error adding tirple : "+quadTriple);
			System.out.println(e.toString());
		}
		
		return model;
	}
	
	private static int counter = 0;
	private TranslationRules toTranslationRules(List<Statement> statements, String datasourceId) {
		TranslationRules translationRules = new TranslationRules();
		translationRules.setDatasourceId(datasourceId);
		translationRules.setId(UUID.randomUUID().toString());
		
		for(int index=0; index < statements.size(); index++) {
			Statement statement = statements.get(index);
			if(index == 0) {
				String subject = statement.getSubject().toString();
				if(subject.startsWith("_:"))
					subject = "http://test.es/resource/"+(counter++);
				translationRules.setSubject(normaliseTripleElement(subject));
			
				
			}
			TranslationRule rule = toTranslationRule(statement);
			translationRules.getProperties().add(rule);
		}
		
		return translationRules;
	}
	
	private TranslationRule toTranslationRule(Statement statement) {
		TranslationRule tr = new TranslationRule();
		tr.setPredicate(normaliseTripleElement(statement.getPredicate().toString()));
		RDFNode object = statement.getObject();
		
		if(object.isLiteral()) {
			tr.setIsLiteral(true);
			Literal literal = object.asLiteral();
			tr.setObject(normaliseTripleElement(literal.getLexicalForm()));
			if(!literal.getLanguage().isEmpty()) {
				tr.setLanguage(literal.getLanguage());
			}else {
				tr.setDataType(literal.getDatatypeURI());
			}
		}else {
			tr.setIsLiteral(false);
			tr.setObject(normaliseTripleElement(object.asResource().getURI()));
		}
		return tr;
	}
	
	private String normaliseTripleElement(String elem) {
		if(elem.contains("∂") && elem.contains("ß"))
			return elem.replace('∂', '{').replace('ß', '}');
		return elem;
	}
	
}
