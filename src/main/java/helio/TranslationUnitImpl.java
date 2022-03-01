package helio;



import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import helio.blueprints.components.AsyncDataProvider;
import helio.blueprints.exceptions.ExtensionNotFoundException;
import helio.blueprints.exceptions.IncorrectMappingException;
import helio.blueprints.mappings.Datasource;
import helio.blueprints.mappings.Expresions;
import helio.blueprints.mappings.TranslationRules;
import helio.blueprints.mappings.TranslationUnit;
import helio.blueprints.mappings.UnitType;
import helio.components.handlers.RDFHandler;
import sparql.streamline.core.SparqlEndpoint;

class TranslationUnitImpl implements TranslationUnit {

	// todo: here the problem is that all translation units will have the same threadpool 
	protected static int threads = 30;
	private static ExecutorService service = Executors.newFixedThreadPool(threads);
	Logger logger = LoggerFactory.getLogger(TranslationUnitImpl.class);

	private String id;
	private String representation;
	private Datasource datasource;
	private Set<String> dataReferences = new HashSet<>();
	private UnitType type;
	private String velocityTemplateName;
	private String subjectRegex;

	private static Map<String, List<String>> linkMatrix = new HashMap<>();
	private Boolean markedForLinking = false;

	private SparqlEndpoint endpoint;
	
	public TranslationUnitImpl(SparqlEndpoint endpoint, Datasource datasource, TranslationRules rules, Boolean markedForLinking) throws IncorrectMappingException, ExtensionNotFoundException{
		this.endpoint = endpoint;
		this.markedForLinking = markedForLinking;
		this.datasource = datasource;
		instantiateUnitType();
		if(datasource.getDataHandler() instanceof RDFHandler) {
			subjectRegex = ".+";
		}else {
			this.dataReferences.addAll(Expresions.extractDataReferences(rules));
			velocityTemplateName = VelocityEvaluator.registerVelocityTemplate(rules);
			subjectRegex = rules.getSubject();
			Expresions.extractDataReferences(rules.getSubject()).parallelStream().forEach(dReference -> subjectRegex.replaceAll(dReference, ".+"));
		}
		id = UUID.randomUUID().toString();
		representation = Utils.concatenate("id: ",id," translates datasource '",datasource.getId(),"' with rules '", rules.getId(),"'");
		// TODO:linking
	}

	private void instantiateUnitType() {
		try {
			if(datasource.getDataProvider() instanceof AsyncDataProvider) {
				((AsyncDataProvider) datasource.getDataProvider()).setTranslationUnit(this);
				type = UnitType.Asyc;
			}else if(datasource.getRefresh()!=null && datasource.getRefresh()>0) {
				type = UnitType.Scheduled;
			}else {
				type = UnitType.Sync;
			}
		}catch(Exception e) {
			logger.error(e.toString());
		}
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean generatesSubject(String subject) {
		return subject.matches(subjectRegex);
	}

	@Override
	public UnitType getUnitType() {
		return type;
	}

	@Override
	public void translate() {
		try {
			InputStream stream = datasource.getDataProvider().getData();
			translate(stream);
		}catch(Exception e) {
			logger.error(e.toString());
		}
	}

	private void translateRDF() {
		//datasource.getDataHandler().splitData()
	}

	@Override
	public void translate(InputStream stream) {
		try {
			if(datasource.getDataHandler() instanceof RDFHandler) {
				//TODO: prepare RDF query
				translateRDF();
			}else {
				datasource.getDataHandler().splitData(stream).parallelStream()
				.map(chunk -> toTranslationMatrix(chunk))
				.map(matrix -> solveMatrix(matrix))
				.forEach(query -> sendQuery(query));
				service.awaitTermination(5, TimeUnit.SECONDS);
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
	}

	/**
	 * The matrix has has column header the dataReference, and as cell a list of values extracted from the raw data
	 * @param chunk
	 * @return
	 */
	private Map<String, List<String>> toTranslationMatrix(String chunk) {
		return this.dataReferences.parallelStream()
				.map(reference -> toMatrixColumn(reference, chunk))
				.map(column -> markForLinking(column))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	private Entry<String, List<String>> toMatrixColumn(String reference, String chunk) {
		List<String> cleanedValues = new ArrayList<>();

		try {
			cleanedValues = this.datasource.getDataHandler().filter(reference, chunk).parallelStream().filter(elem -> elem!=null).map(str-> str.toString().replaceAll("\"", "\\\\\"")).collect(Collectors.toList());
		}catch(Exception e) {
			logger.error(e.toString());
			logger.error(reference);
			logger.error(chunk);
		}
		return new AbstractMap.SimpleEntry<>(reference, cleanedValues);
	}

	private Entry<String,List<String>> markForLinking(Entry<String, List<String>> column){
		// add the column to the linking column
		if(markedForLinking)
			linkMatrix.put(column.getKey(), column.getValue());
		return column;
	}

	private String solveMatrix(Map<String, List<String>> matrix) {
		return (VelocityEvaluator.evaluateTemplate(velocityTemplateName, matrix)).toString();
	}


	private void sendQuery(String query) {
		//System.out.println(query);
		Thread th = new Thread(){
		    @Override
			public void run() {
		    	try {
		    		endpoint.update(query);
				} catch (Exception e) {
					logger.error(query);
					logger.error(e.toString());
				} 
		    }
		};
		service.submit(th);
	}

	@Override
	public String toString() {
		return representation;
	}
}
