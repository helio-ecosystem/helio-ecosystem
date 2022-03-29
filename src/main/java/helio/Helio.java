package helio;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import helio.blueprints.components.TranslationUnit;
import helio.blueprints.components.UnitType;



/**
 * This class implements the methods required to generate RDF either following a synchronous or an asynchronous approach
 * @author Andrea Cimmino
 *
 */
public class Helio extends AbstractHelio {

	public static Logger logger = LoggerFactory.getLogger(Helio.class);
	
	private Set<AsyncronousTranslationTask> asyncExecutors = new HashSet<>();
	private Set<SyncronousTranslationTask> syncExecutors= new HashSet<>();
	private Set<ScheduledTranslationTask> schedExecutors= new HashSet<>();


	//TODO: add static or attribute from which translationUnits can publish events
	/**
	 * This constructor creates a Helio object
	 */
	public Helio() {
		super();
		AbstractHelio.setSilentAcceptanceOfUnknownDatatypes(true);
		AbstractHelio.setEagerLiteralValidation(false);
		AbstractHelio.setbNodeUIDGeneration(true);
		AbstractHelio.setOwlRuleOverOWLRuleWarnings(true);
	}

	
	
	// -- 
	
	
		
	public void add(TranslationUnit unit) {
		if(unit.getUnitType().equals(UnitType.Asyc)) {
			asyncExecutors.add(AsyncronousTranslationTask.create(unit));
		}else if(unit.getUnitType().equals(UnitType.Scheduled)) {
			schedExecutors.add(ScheduledTranslationTask.create(unit, unit.getScheduledTime()));
		}else {
			syncExecutors.add(SyncronousTranslationTask.create(unit));
		}
	}

	
	

	public  List<TranslationUnit> getSyncronousUnits() {
		return syncExecutors.parallelStream().map(exec -> exec.getTranslationUnit()).collect(Collectors.toList());
	}
	
	public void clearExecutors() {
		syncExecutors.clear();
		asyncExecutors.clear();
		schedExecutors.clear();
	}

	public  Model runSynchronous() {
		Model model = ModelFactory.createDefaultModel();
		syncExecutors.parallelStream().map(sync -> sync.getTranslationUnit().translate()).forEach(modelAux -> model.add(model));
		return model;
	}

}
