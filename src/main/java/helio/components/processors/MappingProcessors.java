package helio.components.processors;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import helio.blueprints.Component;
import helio.blueprints.Components;
import helio.blueprints.components.MappingProcessor;
import helio.blueprints.exceptions.ExtensionNotFoundException;
import helio.blueprints.exceptions.IncompatibleMappingException;
import helio.blueprints.exceptions.IncorrectMappingException;
import helio.blueprints.exceptions.MappingExecutionException;
import helio.blueprints.mappings.TripleMapping;

public class MappingProcessors {

	private MappingProcessors() {
		super();
	}
	
	public static Set<TripleMapping> processMapping(String processorName, String mapping) throws ExtensionNotFoundException, IncompatibleMappingException, MappingExecutionException, IncorrectMappingException{
		MappingProcessor processor = Components.getMappingProcessors().get(processorName);
		if(processor==null)
			throw new ExtensionNotFoundException("Processor name belongs to a class not registered as a component");
			
		return processor.parseMapping(mapping);
		
	}
	
	public static Set<TripleMapping> processMapping(String mapping) throws IncompatibleMappingException {
		Set<Entry<String,MappingProcessor>> processors = Components.getMappingProcessors().entrySet();
		Set<TripleMapping> triplets = processors.parallelStream()
				.map(entry -> toTriples(entry.getValue(), mapping))
				.flatMap(set -> set.stream())
				.collect(Collectors.toSet());
		if(triplets.size() == 0)
			throw new IncompatibleMappingException("Provided mapping is not compatible with any registered processor");
		return triplets;
	}
	
	private static Set<TripleMapping> toTriples(MappingProcessor processor, String content){
		try {
			return processor.parseMapping(content);			
		}catch(Exception e) {
			
		}
		return new HashSet<TripleMapping>();
	}
	
}

