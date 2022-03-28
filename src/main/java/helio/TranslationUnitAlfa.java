package helio;



import java.io.InputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
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
import helio.blueprints.mappings.TripleMapping;
import helio.blueprints.mappings.VelocityEvaluator;
import helio.blueprints.objects.TranslationUnit;
import helio.blueprints.objects.UnitType;
import helio.components.handlers.RDFHandler;
import sparql.streamline.core.SparqlEndpoint;

public class TranslationUnitAlfa implements TranslationUnit{

	Logger logger = LoggerFactory.getLogger(TranslationUnitAlfa.class);

	// -- Attributes
	
	private String id;
	private Datasource datasource;
	private UnitType type;
	private Set<String> dataReferences = new HashSet<>();

	private SparqlEndpoint endpoint;
	
	private int threads = 30;
	private ExecutorService service = Executors.newFixedThreadPool(threads);
	
	// -- Constructor
	
	public TranslationUnitAlfa(SparqlEndpoint endpoint, TripleMapping mapping, int threads) throws IncorrectMappingException, ExtensionNotFoundException {
		this.threads = threads;
		init(null, endpoint,  mapping);
	}
	public TranslationUnitAlfa(SparqlEndpoint endpoint, TripleMapping mapping) throws IncorrectMappingException, ExtensionNotFoundException{
		init(null, endpoint,  mapping);
	}
	
	private void init(String id, SparqlEndpoint endpoint, TripleMapping mapping) throws IncorrectMappingException, ExtensionNotFoundException {
		
		
		this.endpoint = endpoint;
		this.datasource = mapping.getDatasource();
		instantiateUnitType();
		
		if(!(datasource.getDataHandler() instanceof RDFHandler))
			this.dataReferences.addAll(mapping.getDataReferences());
		
		this.id=id;
		if(this.id==null)
			this.id = Utils.concatenate("urn:id:"+String.valueOf(this.hashCode()).replace("-", "A"));
		if(mapping.getTemplate()!=null)
			VelocityEvaluator.registerVelocityTemplate(this.id, mapping.getTemplate());

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

	// -- Getters and Setters
	
	public Datasource getDatasource() {
		return datasource;
	}

	public void setDatasource(Datasource datasource) {
		this.datasource = datasource;
	}

	@Override
	public String getId() {
		return id;
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

	@Override
	public void translate(InputStream stream) {
		long startTime = System.currentTimeMillis();
		try {
			if(datasource.getDataHandler() instanceof RDFHandler) {
				//TODO: prepare RDF query
				translateRDF();
			}else {
				datasource.getDataHandler().splitData(stream).parallelStream()
				.map(chunk -> toTranslationMatrix(chunk))
				.map(matrix -> solveMatrix(matrix))
				.forEach(nt -> sendQuery(nt));
				service.awaitTermination(1, TimeUnit.NANOSECONDS);
				//service.shutdown();
			}
		} catch (Exception e) {
			logger.error(e.toString());
		}
		long endTime = (System.currentTimeMillis() - startTime);
		logger.debug("translation " + (endTime - startTime) + " milliseconds");
	}

	private void translateRDF() {
		//datasource.getDataHandler().splitData()
	}
	
	/**
	 * The matrix has has column header the dataReference, and as cell a list of values extracted from the raw data
	 * @param chunk
	 * @return
	 */
	private Map<String, List<String>> toTranslationMatrix(String chunk) {
		Map<String, List<String>> v =  this.dataReferences.parallelStream()
				.map(reference -> toMatrixColumn(reference, chunk))
				//.map(column -> markForLinking(column))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		return v;
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


	private String solveMatrix(Map<String, List<String>> matrix) {
		return (VelocityEvaluator.evaluateTemplate(this.id, matrix)).toString();
	}

	
	private void sendQuery(String nt) {
		//System.out.println(query);
		long startTime = System.currentTimeMillis();
		String query = Utils.concatenate("CLEAR GRAPH <",this.id,">; INSERT { GRAPH <",this.id,"> { \n",nt,"\n} } WHERE {?s ?p ?o }");
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
		long endTime = (System.currentTimeMillis() - startTime);
		logger.debug(Utils.concatenate("Query:",String.valueOf(endTime)," ms"));
	}

	@Override
	public String toString() {
		return "";
	}
	@Override
	public int hashCode() {
		return Objects.hash(dataReferences, datasource, endpoint, type);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TranslationUnitAlfa other = (TranslationUnitAlfa) obj;
		return Objects.equals(dataReferences, other.dataReferences) && Objects.equals(datasource, other.datasource)
				&& Objects.equals(endpoint, other.endpoint)
				&& type == other.type;
	}

	

}
