package helio;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import helio.blueprints.objects.TranslationUnit;
import helio.blueprints.objects.UnitType;



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
	 * This constructor creates a Helio object with the default {@link Configuration}
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
			schedExecutors.add(ScheduledTranslationTask.create(unit, unit.getDatasource().getRefresh()));
		}else {
			syncExecutors.add(SyncronousTranslationTask.create(unit));
		}
	}

	
	public  boolean remove(String translationUnitId) {
		boolean found = false;
		Optional<SyncronousTranslationTask> opt2 = syncExecutors.parallelStream().filter(exec -> exec.getTranslationUnit().getId().equals(translationUnitId)).findFirst();
		if(opt2.isPresent()) {
			found = true;
			syncExecutors.remove(opt2.get());
		}else {
			Optional<AsyncronousTranslationTask> opt1 = asyncExecutors.parallelStream().filter(exec -> exec.getTranslationUnit().getId().equals(translationUnitId)).findFirst();
			if(opt1.isPresent()) {
				found = true;
				asyncExecutors.remove(opt1.get());
			}else {
				Optional<ScheduledTranslationTask> opt3 = schedExecutors.parallelStream().filter(exec -> exec.getTranslationUnit().getId().equals(translationUnitId)).findFirst();
				if(opt3.isPresent()) {
					found = true;
					schedExecutors.remove(opt3.get());
				}
			}
		}
		return found;
	}

	public  List<String> listSyncronousIds() {
		return syncExecutors.parallelStream().map(exec -> exec.getTranslationUnit().getId()).collect(Collectors.toList());
	}

	public  List<String> listAsyncronousIds() {
		return asyncExecutors.parallelStream().map(exec -> exec.getTranslationUnit().getId()).collect(Collectors.toList());
	}

	public  List<String> listScheduledIds() {
		return schedExecutors.parallelStream().map(exec -> exec.getTranslationUnit().getId()).collect(Collectors.toList());
	}
	
	public void clearExecutors() {
		syncExecutors.clear();
		asyncExecutors.clear();
		schedExecutors.clear();
	}

	public  List<String> listIds() {
		List<String> ids = listSyncronousIds();
		ids.addAll(listAsyncronousIds());
		ids.addAll(listScheduledIds());
		return ids;
	}


	public  void runSynchronous() {
		long startTime = System.currentTimeMillis();
		syncExecutors.parallelStream().forEach(sync -> sync.run());
		long endTime = System.currentTimeMillis();
		System.out.println("That took " + (endTime - startTime) + " milliseconds");
	}

}
