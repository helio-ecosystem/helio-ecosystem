package helio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import helio.blueprints.exceptions.ExtensionNotFoundException;
import helio.blueprints.exceptions.IncorrectMappingException;
import helio.blueprints.mappings.Datasource;
import helio.blueprints.mappings.LinkRule;
import helio.blueprints.mappings.Mapping;
import helio.blueprints.mappings.TranslationRules;
import helio.blueprints.mappings.TranslationUnit;
import helio.blueprints.mappings.UnitType;



/**
 * This class implements the methods required to generate RDF either following a synchronous or an asynchronous approach
 * @author Andrea Cimmino
 *
 */
public class Helio {

	public static Logger logger = LoggerFactory.getLogger(Helio.class);

	private Set<AsyncronousTranslationTask> asyncExecutors = new HashSet<>();
	private Set<SyncronousTranslationTask> syncExecutors= new HashSet<>();
	private Set<ScheduledTranslationTask> schedExecutors= new HashSet<>();

	protected List<LinkRule> linkingRules = new ArrayList<>();
	//TODO: add static or attribute from which translationUnits can publish events
	public Helio() {
		super();
	}



	private  void registerTranslationUnit(TranslationUnit unit, String subject) {
		if(unit.getUnitType().equals(UnitType.Asyc)) {
			asyncExecutors.add(AsyncronousTranslationTask.create(unit));
		}else if(unit.getUnitType().equals(UnitType.Scheduled)) {

			schedExecutors.add(ScheduledTranslationTask.create(unit));
		}else {
			syncExecutors.add(SyncronousTranslationTask.create(unit));
		}
	}

	public void createFrom(Mapping mapping) throws IncorrectMappingException, ExtensionNotFoundException{
		mapping.checkMapping();
		
		linkingRules.addAll(mapping.getLinkRules());
		List<Datasource> datasources = mapping.getDatasources();
		List<TranslationRules> translationRulesList = mapping.getTranslationRules();
		for (Datasource datasource : datasources) {
			for (TranslationRules translationRule : translationRulesList) {
				if(translationRule.hasDataSourceId(datasource.getId())) {
					boolean markedForLinking = mapping.getLinkRules().stream().anyMatch(lrules -> lrules.getSourceNamedGraph().equals(translationRule.getId()) || lrules.getTargetNamedGraph().equals(translationRule.getId()));
					try {
						TranslationUnit unit = new TranslationUnitImpl(datasource, translationRule, markedForLinking);
						registerTranslationUnit(unit, translationRule.getSubject());
					}catch(Exception e) {
						logger.error(e.toString());
					}
				}
			}
		}

	}

	public  boolean delete(String id) {
		boolean found = false;
		Optional<SyncronousTranslationTask> opt2 = syncExecutors.parallelStream().filter(exec -> exec.getTranslationUnit().getId().equals(id)).findFirst();
		if(opt2.isPresent()) {
			found = true;
			syncExecutors.remove(opt2.get());
		}else {
			Optional<AsyncronousTranslationTask> opt1 = asyncExecutors.parallelStream().filter(exec -> exec.getTranslationUnit().getId().equals(id)).findFirst();
			if(opt1.isPresent()) {
				found = true;
				asyncExecutors.remove(opt1.get());
			}else {
				Optional<ScheduledTranslationTask> opt3 = schedExecutors.parallelStream().filter(exec -> exec.getTranslationUnit().getId().equals(id)).findFirst();
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

	public  List<String> listIds() {
		List<String> ids = listSyncronousIds();
		ids.addAll(listAsyncronousIds());
		ids.addAll(listScheduledIds());
		return ids;
	}


	public  void runSynchronous() {
		syncExecutors.parallelStream().forEach(sync -> sync.run());
	}

	public  void runSynchronous(String subject) {
		syncExecutors.parallelStream().filter(unit -> unit.getTranslationUnit().generatesSubject(subject)).forEach(sync -> sync.run());
	}



}
