package helio;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;
import com.jayway.jsonpath.internal.Utils;

import helio.bleprints.mappings.Mapping;
import helio.blueprints.components.MappingReader;
import helio.blueprints.exceptions.IncompatibleMappingException;
import helio.blueprints.exceptions.IncorrectMappingException;
import helio.components.loader.Extensions;

public class Mappings extends AbstractIgnition  {

	private static Logger logger = LoggerFactory.getLogger(Mappings.class);



	public static Mapping readMapping(String rawMapping) {
		Mapping mapping = null;
		List<MappingReader> readers = Lists.newArrayList(Extensions.mappingReaders.values());
		if(readers.isEmpty())
			throw new IllegalArgumentException("No mapping readers are loadded in Helio currently");
		for (MappingReader reader : readers) {
			try {
				long startTime = System.nanoTime();
				mapping = reader.readMapping(rawMapping);
				long endTime = System.nanoTime();
				long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
				logger.debug(Utils.concat("Parsing mapping with ", reader.getClass().getName()," in ", String.valueOf(duration), " ms"));
				break;
			} catch (IncompatibleMappingException e) {
				logger.warn("mapping not compatible with "+reader.getClass().getCanonicalName());
			} catch( IncorrectMappingException e) {
				logger.error("Mapping has syntax errors");
			}
		}
		return mapping;
	}




}
