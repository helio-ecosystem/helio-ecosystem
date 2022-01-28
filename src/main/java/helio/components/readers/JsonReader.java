package helio.components.readers;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import helio.Utils;
import helio.bleprints.mappings.Datasource;
import helio.bleprints.mappings.Mapping;
import helio.blueprints.components.DataHandler;
import helio.blueprints.components.DataProvider;
import helio.blueprints.components.MappingReader;
import helio.blueprints.exceptions.IncompatibleMappingException;
import helio.blueprints.exceptions.IncorrectMappingException;
import helio.components.loader.Extensions;

public class JsonReader implements MappingReader {

	private static final String KEY_TYPE = "type";


	@Override
	public Mapping readMapping(String content) throws IncompatibleMappingException {
		try {
			Mapping mapping =  Utils.GSON.fromJson(content, Mapping.class);
			List<Datasource> dss = mapping.getDatasources().parallelStream().map(ds -> instantiateDatasource(ds)).collect(Collectors.toList());
			mapping.setDatasources(dss);
			return mapping;
		} catch(Exception e) {
			e.printStackTrace();
			throw new IncompatibleMappingException("Provided mapping is not compatible with JsonReader: "+content);
		}
	}

	private Datasource instantiateDatasource(Datasource ds) {
		DataProvider provider = packProvider(ds.getProviderConfiguration());
		DataHandler handler = packHandler(ds.getHandlerConfiguration());
		ds.setDataHandler(handler);
		ds.setDataProvider(provider);
		return ds;
	}

	private DataProvider packProvider(JsonObject json) {
		DataProvider provider = null;
		try {
			if (!json.has(KEY_TYPE)) {
				throw new IncorrectMappingException(
						"the JSON document for the provider must contain the mandatory key 'type' with a correct value");
			} else {
				String name = json.get(KEY_TYPE).getAsString();
				if (name != null && !name.isEmpty()) {
					provider = Extensions.dataProviders.get(name);
					if (provider == null)
						throw new IncorrectMappingException("Data provider specified in the mapping does not exist");
					provider.configure(json);
				} else {
					throw new IncorrectMappingException("Value of key 'type' can not be null or blank");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return provider;
	}


	private DataHandler packHandler(JsonObject json) {
		DataHandler handler = null;
		try {
			if (!json.has(KEY_TYPE)) {
				throw new IncorrectMappingException(
						"the JSON document for the provider must contain the mandatory key 'type' with a correct value");
			} else {
				String name = json.get(KEY_TYPE).getAsString();
				if (name != null && !name.isEmpty()) {
					handler = Extensions.dataHandlers.get(name);
					if (handler == null)
						throw new IncorrectMappingException("Data handler specified in the mapping does not exist");
					handler.configure(json);
				} else {
					throw new IncorrectMappingException("Value of key 'type' can not be null or blank");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return handler;
	}

}
