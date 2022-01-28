package helio.components.readers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.javatuples.Quartet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import helio.Helio;
import helio.bleprints.mappings.Datasource;
import helio.bleprints.mappings.LinkRule;
import helio.bleprints.mappings.Mapping;
import helio.bleprints.mappings.TranslationRule;
import helio.bleprints.mappings.TranslationRules;
import helio.blueprints.components.DataHandler;
import helio.blueprints.components.DataProvider;
import helio.blueprints.components.MappingReader;
import helio.blueprints.exceptions.IncompatibleMappingException;
import helio.blueprints.exceptions.IncorrectMappingException;
import helio.components.handlers.CsvHandler;
import helio.components.handlers.JsonHandler;
import helio.components.handlers.XmlHandler;
import helio.components.providers.FileProvider;
import helio.components.providers.URLProvider;

/**
 * This class implements a {@link MappingTranslator} that translates <a href="https://rml.io/specs/rml/">RML mappings</a> into a valid {@link Mapping}
 * @author Andrea Cimmino
 *
 */
public class RmlReader implements MappingReader {



	private static ValueFactory factory = SimpleValueFactory.getInstance();

	private static final IRI RML_LOGICAL_SOURCE_PROPERTY = factory.createIRI("http://semweb.mmlab.be/ns/rml#logicalSource");
    private static final IRI RML_SOURCE_PROPERTY = factory.createIRI("http://semweb.mmlab.be/ns/rml#source");
    private static final IRI RML_REFERENCE_FORMULATION_PROPERTY = factory.createIRI("http://semweb.mmlab.be/ns/rml#referenceFormulation");
    private static final IRI RML_ITERATOR_PROPERTY = factory.createIRI("http://semweb.mmlab.be/ns/rml#iterator");

    private static final IRI RML_SUBJECT_PROPERTY = factory.createIRI("http://www.w3.org/ns/r2rml#subject");
    private static final IRI RML_SUBJECTMAP_PROPERTY = factory.createIRI("http://www.w3.org/ns/r2rml#subjectMap");
    private static final IRI RML_SUBJECT_CLASS_PROPERTY = factory.createIRI("http://www.w3.org/ns/r2rml#class");


    private static final IRI RML_PREDICATE_OBJECT_MAP_PROPERTY = factory.createIRI("http://www.w3.org/ns/r2rml#predicateObjectMap");
    private static final IRI RML_PREDICATE_PROPERTY = factory.createIRI("http://www.w3.org/ns/r2rml#predicate");
    private static final IRI RML_PREDICATE_MAP_PROPERTY = factory.createIRI("http://www.w3.org/ns/r2rml#predicateMap");
    private static final IRI RML_OBJECT_PROPERTY = factory.createIRI("http://www.w3.org/ns/r2rml#OBJECT");
    private static final IRI RML_OBJECT_MAP_PROPERTY = factory.createIRI("http://www.w3.org/ns/r2rml#objectMap");


    private static final IRI RML_IRI_TEMPLATE_PROPERTY = factory.createIRI("http://www.w3.org/ns/r2rml#template");
   	private static final IRI RML_IRI_CONSTANT_PROPERTY = factory.createIRI("http://www.w3.org/ns/r2rml#constant");
    private static final IRI RML_IRI_REFERENCE_PROPERTY = factory.createIRI("http://semweb.mmlab.be/ns/rml#reference");
    private static final IRI RML_LITERAL_REFERENCE_PROPERTY = factory.createIRI("http://www.w3.org/ns/r2rml#Literal");
    private static final IRI RML_PARENT_TRIPLEMAP_PROPERTY = factory.createIRI("http://www.w3.org/ns/r2rml#parentTriplesMap");
    private static final IRI RML_JOIN_CONDITION = factory.createIRI("http://www.w3.org/ns/r2rml#joinCondition");
    private static final IRI RML_CHILD = factory.createIRI("http://www.w3.org/ns/r2rml#child");
    private static final IRI RML_PARENT = factory.createIRI("http://www.w3.org/ns/r2rml#parent");



    private static final IRI RML_DATATYPE  = factory.createIRI("http://www.w3.org/ns/r2rml#datatype");
    private static final IRI RML_LANG  = factory.createIRI("http://www.w3.org/ns/r2rml#language");


    private static final IRI RML_QL_JSONPATH = factory.createIRI("http://semweb.mmlab.be/ns/ql#JSONPath");
    private static final IRI RML_QL_CSV = factory.createIRI("http://semweb.mmlab.be/ns/ql#CSV");
    private static final IRI RML_QL_XPATH = factory.createIRI("http://semweb.mmlab.be/ns/ql#XPath");
	private static final IRI[] CONTEXTS = new IRI[] {};

	private static final String TOKEN_PROPERTIES = " properties";
	Logger logger = LoggerFactory.getLogger(RmlReader.class);

	private List<LinkRule> LinkRules;

	/**
	 * This constructor initializes the {@link RmlReader}
	 */
	public RmlReader() {
		LinkRules = new ArrayList<>();
	}



	@Override
	public Mapping readMapping(String content)  throws IncompatibleMappingException, IncorrectMappingException {
		Mapping mapping = new Mapping();

		try(InputStream inputStream = new ByteArrayInputStream(content.getBytes())){
			Model model = Rio.parse(inputStream, Helio.configuration.getNamespace(), RDFFormat.TURTLE);
			mapping = parseMapping(model);
		} catch (Exception e) {
			//logger.error(e.toString());
			throw new IncompatibleMappingException(e.toString());
		}
		return mapping;
	}

	private Mapping parseMapping(Model model) {
		Mapping mapping = new Mapping();

		Iterable<Statement> statements = model.getStatements(null, RML_LOGICAL_SOURCE_PROPERTY, null, CONTEXTS);
		statements.forEach(st -> {
			try {
				String DatasourceId = st.getObject().stringValue();
				Datasource Datasource = parseDatasource( st.getObject(), model);
				mapping.getDatasources().add(Datasource);
				String format = extractDataFormat(Datasource);
				TranslationRules translationRules =  parseTranslationTranslationRules( st.getSubject(), model, DatasourceId, format);
				if(translationRules.getProperties().isEmpty())
					throw new IncorrectMappingException("Povided mapping generates no triples, maybe it only relies on the "+RML_PARENT_TRIPLEMAP_PROPERTY+", generate at least a rr:class triple in order to correctly function");
				mapping.getTranslationRules().add(translationRules);
			} catch (IncorrectMappingException e) {
				logger.error(e.toString());
			}
		});

		mapping.getLinkRules().addAll(LinkRules);

		return mapping;
	}

	private String extractDataFormat(Datasource ds ) {
		return  ds.getDataHandler().getClass().getSimpleName().replace("Handler", "").toLowerCase();
	}


	// Parsing the TranslationTranslationRules

	private TranslationRules parseTranslationTranslationRules(Value documentRootIRI, Model model, String DatasourceId, String format) throws IncorrectMappingException {
		// subject template
		String evaluableMappingSubject = extractEvaluableMappingSubject(documentRootIRI, model, format);
		String translationRulesId = documentRootIRI.stringValue();

		TranslationRules translationRules = new TranslationRules();
		translationRules.getDatasourcesId().add(DatasourceId);
		translationRules.setId(translationRulesId);
		translationRules.setSubject(evaluableMappingSubject);

		translationRules.getProperties().addAll(extractTranslationRules(documentRootIRI, model, format, translationRulesId));


		return translationRules;
	}

	private List<TranslationRule> extractTranslationRules(Value documentRootIRI, Model model, String format, String TranslationTranslationRulesId){
		List<TranslationRule> TranslationRules = new ArrayList<>();
		// the rr:class
		 TranslationRule typeTranslationRule = extractRdfTypeTranslationRule(documentRootIRI, model, format);
		 if(typeTranslationRule!=null)
			 TranslationRules.add(typeTranslationRule);
		// the other properties
		List<Value> propertyObjectMapsSubjects = getRangeValues(documentRootIRI, RML_PREDICATE_OBJECT_MAP_PROPERTY, model);
		for(int index=0; index < propertyObjectMapsSubjects.size(); index++) {
			Value propertyObjectMapsSubject = propertyObjectMapsSubjects.get(index);
			try {
				TranslationRule newTranslationRule = extractObjectMapTranslationRule(propertyObjectMapsSubject, model, format, TranslationTranslationRulesId);
				if(newTranslationRule.getPredicate()!=null && newTranslationRule.getObject()!=null)
					TranslationRules.add(newTranslationRule);
			}catch(Exception e) {
				e.printStackTrace();
				logger.error(e.toString());
			}
		}

		return TranslationRules;
	}

	private TranslationRule extractObjectMapTranslationRule(Value objectMapSubject, Model model, String format, String TranslationTranslationRulesId) throws IncorrectMappingException {
		TranslationRule newTranslationRule = new TranslationRule();
		int LinkRulesSize = this.LinkRules.size();
		String predicate = extractPredicate(objectMapSubject, model, format);
		Quartet<String,String, String, Boolean> object = extractObject(objectMapSubject, model, format, TranslationTranslationRulesId);
		if(LinkRulesSize==this.LinkRules.size() && object.getValue(0)!=null) {
			newTranslationRule.setPredicate(predicate);
			newTranslationRule.setObject(object.getValue0().toString());
			if(object.getValue1()!=null)
				newTranslationRule.setDataType(object.getValue1());
			if(object.getValue2()!=null)
				newTranslationRule.setLanguage(object.getValue2());
			newTranslationRule.setIsLiteral(object.getValue3());
		}else if(LinkRulesSize+1==this.LinkRules.size() && object.getValue(0)==null) {
			logger.warn("New link TranslationRule added");
		}else {
			throw new IncorrectMappingException("An unexpected error occured in line 237");
		}



		return newTranslationRule;
	}

	private Quartet<String,String, String, Boolean> extractObject(Value objectPredicateMapSubject, Model model, String format,  String TranslationTranslationRulesId) throws IncorrectMappingException {
		String objectTemplate =  getUnitaryRange(objectPredicateMapSubject, RML_OBJECT_PROPERTY, model);
		Quartet<String,String, String, Boolean> quarted = Quartet.with(objectTemplate, null, null, false);
		if(objectTemplate==null) {
			Value objectMapSubject = getUnitaryRangeValue(objectPredicateMapSubject, RML_OBJECT_MAP_PROPERTY, model);
			if(objectMapSubject==null) {
				throw new IncorrectMappingException(concatStrings("Missing object to generate the RDF, specify it using either ",RML_OBJECT_PROPERTY.toString()," or ",RML_OBJECT_MAP_PROPERTY.toString(), TOKEN_PROPERTIES));
			}else {
				objectTemplate = getUnitaryRange(objectMapSubject, RML_IRI_REFERENCE_PROPERTY, model);
				if(objectTemplate==null) {
					// entails this can be an object property
					objectTemplate = getUnitaryRange(objectMapSubject, RML_IRI_TEMPLATE_PROPERTY, model);
					if(objectTemplate!=null) {
						objectTemplate = formatDataAccessIRI(objectTemplate, format); // format the template
					}else {
						objectTemplate = getUnitaryRange(objectMapSubject, RML_IRI_CONSTANT_PROPERTY, model);
					}
					if(objectTemplate!=null) {
						quarted = Quartet.with(objectTemplate, null, null, false);
					}else {
						addProcessableLinkRule(objectPredicateMapSubject, objectMapSubject, model, format, TranslationTranslationRulesId);
					}
				}else {
					quarted = buildQuarted( objectMapSubject,  model,  objectTemplate,  format);
				}
			}
		}
		return quarted;
	}

	private Quartet<String,String, String, Boolean> buildQuarted(Value objectMapSubject, Model model, String objectTemplate, String format) {
		String datatype = getUnitaryRange(objectMapSubject, RML_DATATYPE, model);
		String lang = getUnitaryRange(objectMapSubject, RML_LANG, model);
		objectTemplate = formatDataAccess(objectTemplate, format);
		return Quartet.with(objectTemplate, datatype, lang, true);
	}

	private String formatDataAccess(String objectTemplate, String format) {
		String objectTemplateCopy = objectTemplate;
		if(format.equals("json")) {
			objectTemplateCopy = concatStrings("{$.",objectTemplate,"}");
		}else if(format.equals("xml")) {
			objectTemplateCopy = concatStrings("{//",objectTemplate,"}");
		}else if(format.equals("csv")) {
			objectTemplateCopy = concatStrings("{",objectTemplate,"}");
		}
		return objectTemplateCopy;
	}

	private String formatDataAccessIRI(String iriTemplate, String format) {
		String iriTemplateCopy = iriTemplate;
		if(format.equals("json")) {
			iriTemplateCopy = iriTemplate.replaceAll("\\{", "{\\$.");
		}else if(format.equals("xml")) {
			iriTemplateCopy = iriTemplate.replaceAll("\\{", "{//");
		}
		return iriTemplateCopy;
	}

	private void addProcessableLinkRule(Value objectPredicateMapSubject, Value objectMapSubject, Model model, String format, String TranslationTranslationRulesId) throws IncorrectMappingException {
		Value parentTripleMapValue = getUnitaryRangeValue(objectMapSubject, RML_PARENT_TRIPLEMAP_PROPERTY, model);
		if(parentTripleMapValue!=null) {
			Value joinConditionSubject = getUnitaryRangeValue(objectMapSubject, RML_JOIN_CONDITION, model);
			String predicate = extractPredicate(objectPredicateMapSubject, model, format);
			StringBuilder joinCondition = new StringBuilder();
			String targetFormat = extractDataFormat(parseDatasource(extractLogicalSource(parentTripleMapValue, model), model));
			if(joinConditionSubject!=null) {
				String child = getUnitaryRange(joinConditionSubject, RML_CHILD, model);
				String parent = getUnitaryRange(joinConditionSubject, RML_PARENT, model);
				joinCondition.append("S(").append(formatDataAccess(child, format)).append(") = T(").append(formatDataAccess(parent, targetFormat)).append(")");
			}else {
				joinCondition.append("1 = 1");
			}
			LinkRule TranslationRule = new LinkRule();
			TranslationRule.setExpression(joinCondition.toString());
			TranslationRule.setPredicate(predicate);
			TranslationRule.setSourceNamedGraph(TranslationTranslationRulesId);
			TranslationRule.setTargetNamedGraph(parentTripleMapValue.stringValue());
			LinkRules.add(TranslationRule);
		}else {
			throw new IncorrectMappingException(concatStrings("Missing object specification, specify it using either ",RML_IRI_CONSTANT_PROPERTY.toString(),", ",RML_IRI_REFERENCE_PROPERTY.toString(),", ",RML_IRI_TEMPLATE_PROPERTY.toString()," or", RML_PARENT_TRIPLEMAP_PROPERTY.toString()));
		}
	}


	private Value extractLogicalSource(Value parentTripleMapValue, Model model) {
		return getUnitaryRangeValue(parentTripleMapValue, RML_LOGICAL_SOURCE_PROPERTY, model);
	}

	private String extractPredicate(Value objectMapSubject, Model model, String format) throws IncorrectMappingException {
		String predicateTemplate =  getUnitaryRange(objectMapSubject, RML_PREDICATE_PROPERTY, model);
		if(predicateTemplate==null) {
			Value predicateMapSubject = getUnitaryRangeValue(objectMapSubject, RML_PREDICATE_MAP_PROPERTY, model);
			if(predicateMapSubject==null) {
				throw new IncorrectMappingException(concatStrings("Missing predicate to generate the RDF, specify it using either ",RML_PREDICATE_PROPERTY.toString()," or ",RML_PREDICATE_MAP_PROPERTY.toString(), TOKEN_PROPERTIES));
			}else {
				predicateTemplate = getUnitaryRange(predicateMapSubject, RML_IRI_CONSTANT_PROPERTY, model);
				if(predicateTemplate==null)
					predicateTemplate = getUnitaryRange(predicateMapSubject, RML_IRI_TEMPLATE_PROPERTY, model);
				if(predicateTemplate==null) {
					throw new IncorrectMappingException(concatStrings("Missing predicate specification, specify it using either ",RML_IRI_CONSTANT_PROPERTY.toString()," or ", RML_IRI_TEMPLATE_PROPERTY.toString()));
				}else {
					predicateTemplate = formatDataAccessIRI(predicateTemplate, format);
				}
			}
		}
		return predicateTemplate;
	}

	private TranslationRule extractRdfTypeTranslationRule(Value documentRootIRI, Model model, String format) {
		TranslationRule classTranslationRule = null;
		Value subjectMapSubject = getUnitaryRangeValue(documentRootIRI, RML_SUBJECTMAP_PROPERTY, model);
		if(subjectMapSubject!=null) {
			String rdfTypeIRI = getUnitaryRange(subjectMapSubject, RML_SUBJECT_CLASS_PROPERTY, model);
			if(rdfTypeIRI!=null) {
				classTranslationRule = new TranslationRule();
				classTranslationRule.setIsLiteral(false);
				classTranslationRule.setPredicate("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
				classTranslationRule.setObject(formatDataAccessIRI(rdfTypeIRI, format));
			}
		}
		return classTranslationRule;
	}



	private String extractEvaluableMappingSubject(Value documentRootIRI, Model model, String format) throws IncorrectMappingException {
		String subjectExpression =  getUnitaryRange(documentRootIRI, RML_SUBJECT_PROPERTY, model);
		if(subjectExpression==null) {
			Value subjectMapSubject = getUnitaryRangeValue(documentRootIRI, RML_SUBJECTMAP_PROPERTY, model);
			if(subjectMapSubject==null) {
				throw new IncorrectMappingException(concatStrings("Missing subject to generate the RDF, specify it using either ",RML_SUBJECT_PROPERTY.toString()," or ",RML_SUBJECTMAP_PROPERTY.toString(), TOKEN_PROPERTIES));
			}else {
				subjectExpression = getUnitaryRange(subjectMapSubject, RML_IRI_CONSTANT_PROPERTY, model);
				if(subjectExpression==null)
					subjectExpression = getUnitaryRange(subjectMapSubject, RML_IRI_TEMPLATE_PROPERTY, model);
				if(subjectExpression==null) {
					throw new IncorrectMappingException(concatStrings("Missing subject for triples that will be generated, specify it using either ",RML_IRI_TEMPLATE_PROPERTY.toString()," or "+RML_IRI_CONSTANT_PROPERTY.toString(), TOKEN_PROPERTIES));
				}else {
					subjectExpression = formatDataAccessIRI(subjectExpression, format);
				}
			}
		}
		return subjectExpression;
	}



	// Parsing the Datasource

	private Datasource parseDatasource(Value subject, Model model) throws IncorrectMappingException {

		String source = getUnitaryRange(subject, RML_SOURCE_PROPERTY, model);
		if(source==null) {
			throw new IncorrectMappingException("Provided mapping lacks of mandatory property "+RML_SOURCE_PROPERTY);
		}
		Boolean isFile =!source.startsWith("http") && !source.startsWith("ftp");

		String iterator = getUnitaryRange(subject, RML_ITERATOR_PROPERTY, model);
		String referenceFormulation = getUnitaryRange(subject, RML_REFERENCE_FORMULATION_PROPERTY, model);
		if(referenceFormulation==null)
			throw new IncorrectMappingException("Provided mapping lacks of mandatory property "+RML_REFERENCE_FORMULATION_PROPERTY);

		return buildDatasource(subject.stringValue(), source, referenceFormulation.toLowerCase(), iterator, isFile);

	}

	private Datasource buildDatasource(String id, String source, String referenceFormulation, String iterator, Boolean isFile) throws IncorrectMappingException {
		//
		DataProvider provider = null;
		if(isFile) {
			provider = new FileProvider(new File(source));
		}else {
			provider = new URLProvider(source);
		}
		//
		DataHandler handler = null;
		if(RML_QL_CSV.toString().toLowerCase().contains(referenceFormulation.toLowerCase())) {
			handler = new CsvHandler(",");
			logger.warn("CSV file must have the name of the columns as first row, columns must be separated by ',' and contain no text delimitator");
		}else if(RML_QL_JSONPATH.toString().toLowerCase().contains(referenceFormulation.toLowerCase())) {
			if(iterator ==null || iterator.isEmpty()){
				throw new IncorrectMappingException("JSON format requires an iterator specified with "+RML_ITERATOR_PROPERTY);
			}else {
				handler = new JsonHandler(iterator);
			}
		}else if(RML_QL_XPATH.toString().toLowerCase().contains(referenceFormulation.toLowerCase())) {
			if(iterator ==null || iterator.isEmpty()){
				throw new IncorrectMappingException("Xml format requires an iterator specified with "+RML_ITERATOR_PROPERTY);
			}else {
				handler = new XmlHandler(iterator);
			}
		}else {
			throw new IncorrectMappingException("Current implementation only supports CSV, XML, or JSON");
		}
		//
		return  new Datasource(id, handler, provider);
	}





	// Ancillary methods

	private String getUnitaryRange(Value subject, IRI property, Model model) {
		String output = null;
		Resource subjectIRI = null;
		if(subject instanceof BNode) {
			subjectIRI = factory.createBNode(subject.stringValue());
		}else if( subject instanceof IRI) {
			subjectIRI = factory.createIRI(subject.stringValue());
		}else {
			// throw something ?
		}

		Iterator<Statement> iterator = model.getStatements(subjectIRI, property, null, CONTEXTS).iterator();
		while(iterator.hasNext()) {
			output = iterator.next().getObject().stringValue();
			break;
		}

		return output;
	}

	private Value getUnitaryRangeValue(Value subject, IRI property, Model model) {
		Value output = null;
		Resource subjectIRI = null;
		if(subject instanceof BNode) {
			subjectIRI = factory.createBNode(subject.stringValue());
		}else if( subject instanceof IRI) {
			subjectIRI = factory.createIRI(subject.stringValue());
		}else {
			// throw something ?
		}

		Iterator<Statement> iterator = model.getStatements(subjectIRI, property, null, CONTEXTS).iterator();
		while(iterator.hasNext()) {
			output = iterator.next().getObject();
			break;
		}

		return output;
	}

	private List<Value> getRangeValues(Value subject, IRI property, Model model) {
		List<Value> output = new ArrayList<>();
		Resource subjectIRI = null;
		if(subject instanceof BNode) {
			subjectIRI = factory.createBNode(subject.stringValue());
		}else if( subject instanceof IRI) {
			subjectIRI = factory.createIRI(subject.stringValue());
		}else {
			// throw something ?
		}

		Iterator<Statement> iterator = model.getStatements(subjectIRI, property, null, CONTEXTS).iterator();
		while(iterator.hasNext()) {
			output.add(iterator.next().getObject());
		}

		return output;
	}

	private String concatStrings(String str1, String str2, String str3) {
		StringBuilder builder = new StringBuilder();
		builder.append(str1).append(str2).append(str3);
		return builder.toString();
	}

	private String concatStrings(String str1, String str2, String str3, String str4) {
		StringBuilder builder = new StringBuilder();
		builder.append(str1).append(str2).append(str3).append(str4);
		return builder.toString();
	}

	private String concatStrings(String str1, String str2, String str3, String str4, String str5) {
		StringBuilder builder = new StringBuilder();
		builder.append(str1).append(str2).append(str3).append(str4).append(str5);
		return builder.toString();
	}

	private String concatStrings(String str1, String str2, String str3, String str4, String str5, String str6,String str7, String str8) {
		StringBuilder builder = new StringBuilder();
		builder.append(str1).append(str2).append(str3).append(str4).append(str5).append(str6).append(str7).append(str8);
		return builder.toString();
	}


}
