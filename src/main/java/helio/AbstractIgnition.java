package helio;

import org.apache.jena.shared.impl.JenaParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import helio.components.loader.Extensions;

abstract class AbstractIgnition {

	public static Logger logger = LoggerFactory.getLogger(Helio.class);


	static {
		// Jena configuration
		JenaParameters.enableSilentAcceptanceOfUnknownDatatypes=true;
		JenaParameters.enableEagerLiteralValidation = true;
		JenaParameters.disableBNodeUIDGeneration=true;
		JenaParameters.enableOWLRuleOverOWLRuleWarnings=true;
		try {
			Extensions.registerExtension(null, "helio.components.handlers.JsonHandler", Extensions.EXTENSION_TYPE_HANDLER);
			Extensions.registerExtension(null, "helio.components.handlers.CsvHandler", Extensions.EXTENSION_TYPE_HANDLER);

			Extensions.registerExtension(null, "helio.components.providers.FileProvider", Extensions.EXTENSION_TYPE_PROVIDER);

			Extensions.registerExtension(null, "helio.components.readers.JsonReader", Extensions.EXTENSION_TYPE_READER);
			Extensions.registerExtension(null, "helio.components.readers.RmlReader", Extensions.EXTENSION_TYPE_READER);

			Extensions.registerExtension(null, "helio.components.functions.HF", Extensions.EXTENSION_TYPE_FUNCTION);



		}catch(Exception e) {

		}

		logger.info("Default Helio components loaded");
	}
}
