package helio.components.readers;

import helio.Utils;
import helio.blueprints.components.MappingReader;
import helio.blueprints.exceptions.IncompatibleMappingException;
import helio.blueprints.mappings.Mapping;

public class JsonReader implements MappingReader {



	@Override
	public Mapping readMapping(String content) throws IncompatibleMappingException {
		try {
			return  Utils.GSON.fromJson(content, Mapping.class);
		} catch(Exception e) {
			//e.printStackTrace();
			throw new IncompatibleMappingException("Provided mapping is not compatible with JsonReader: "+e.toString());
		}
	}

	



}
