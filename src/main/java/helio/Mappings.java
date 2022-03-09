package helio;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;

import helio.blueprints.Components;
import helio.blueprints.components.MappingReader;
import helio.blueprints.exceptions.ExtensionNotFoundException;
import helio.blueprints.exceptions.IncompatibleMappingException;
import helio.blueprints.exceptions.IncorrectMappingException;
import helio.blueprints.mappings.Mapping;

public class Mappings  {

	private static Logger logger = LoggerFactory.getLogger(Mappings.class);



	public static Mapping readMapping(String rawMapping) throws IncompatibleMappingException, IncorrectMappingException, ExtensionNotFoundException {
		Mapping mapping = null;
		List<MappingReader> readers = Lists.newArrayList(Components.getMappingReaders().values());
 		String exceptions1 = "";
 		String exceptions2 = "";
		if(readers.isEmpty())
			throw new IllegalArgumentException("No component for reading mappings are loadded currently");
		for (int index=0; index < readers.size(); index++) {
			MappingReader reader = readers.get(index);
			try {
				long startTime = System.nanoTime();
				mapping = reader.readMapping(rawMapping);
				long endTime = System.nanoTime();
				long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
				logger.debug(Utils.concatenate("Parsing mapping with ", reader.getClass().getName()," in ", String.valueOf(duration), " ms"));
				break;
			} catch (IncompatibleMappingException e) {
				logger.warn("mapping not compatible with "+reader.getClass().getCanonicalName());
				exceptions1 = Utils.concatenate(exceptions1, "\n", e.toString());
			} catch( IncorrectMappingException e) {
				logger.error("Mapping has syntax errors");
				exceptions2 = Utils.concatenate(exceptions2, "\n", e.toString());
			} catch (ExtensionNotFoundException e) {
				throw e;
			}
		}
		if(mapping==null && !exceptions1.isEmpty()) {
			throw new IncompatibleMappingException("Mapping provided is not compatible with any reader:\n"+exceptions1);
		}else if(!exceptions2.isEmpty()) {
			throw new IncorrectMappingException("Provided mapping has syntax errors: \n"+exceptions2);
		}
				
		return mapping;
	}




}
