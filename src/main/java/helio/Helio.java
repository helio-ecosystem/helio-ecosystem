package helio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import helio.blueprints.TranslationUnit;
import helio.blueprints.UnitType;

/**
 * This class implements the methods required to generate RDF either following a
 * synchronous or an asynchronous approach
 * 
 * @author Andrea Cimmino
 *
 */
public class Helio {

	public static Logger logger = LoggerFactory.getLogger(Helio.class);

	private ExecutorService scheduledExecutorService;
	private Map<String, PairUnitFuture> futures;
	/**
	 * This constructor creates a Helio object
	 */
	public Helio() {
		super();
		scheduledExecutorService = Executors.newCachedThreadPool();
		futures = new HashMap<>();
	}

	public Helio(int threads) {
		super();
		scheduledExecutorService = Executors.newFixedThreadPool(threads);
		futures = new HashMap<>();
	}
	
	// --

	public void add(TranslationUnit unit) {
		PairUnitFuture uFuture = null;
		if (UnitType.isAsync(unit)) {
			uFuture = new PairUnitFuture(unit, scheduledExecutorService.submit(unit.getTask()));
		} else if (UnitType.isSync(unit)) {
			uFuture = new PairUnitFuture(unit, scheduledExecutorService.submit(unit.getTask()));
		} else {
			
			// TODO: throw exception
		}
		if (uFuture != null)
			this.futures.put(unit.getId(), uFuture);
	}

	public List<TranslationUnit> getTranslationUnits(){
		return this.futures.entrySet().parallelStream().map(entry -> entry.getValue().getUnit()).collect(Collectors.toList());	
	}
	
	public List<TranslationUnit> getFilteredTranslationUnits(UnitType type){
		if(type==null)
			return getTranslationUnits();
		return this.futures.entrySet().parallelStream().map(entry -> entry.getValue().getUnit()).filter(unit -> type.equals(unit.getUnitType())).collect(Collectors.toList());	
	}
		
	public List<String> readAndFlush(String id) {
		List<String> translations = new ArrayList<>();
		PairUnitFuture pair =  this.futures.get(id);
		if(pair!=null) {
			TranslationUnit unit = pair.getUnit();
			if(UnitType.isSync(unit)) {
				try {
					pair.getFuture().get();
					add(pair.getUnit());
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			translations.addAll(unit.getTranslations());
			unit.flush();
		}else {
			//TODO: THROW EXCEPTION
		}
		return translations;
	}
	
	public List<String> readAndFlushAll() {
		return this.futures.entrySet().parallelStream().flatMap(entry -> readAndFlush(entry.getKey()).stream()).collect(Collectors.toList());	
	}

	

	public void stop(String id) {
		PairUnitFuture pair =  this.futures.get(id);
		if(pair!=null) {
			pair.getFuture().cancel(true);
		}else {
			//TODO: THROW EXCEPTION
		}	
	}
	
	public void stopAll() {
		this.futures.entrySet().parallelStream().forEach(entry -> stop(entry.getKey()));
	}

	public void close() {
		this.scheduledExecutorService.shutdownNow();
	}
	
}
