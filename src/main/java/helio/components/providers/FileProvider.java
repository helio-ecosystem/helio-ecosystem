package helio.components.providers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import helio.blueprints.components.DataProvider;

/**
 * This object implements the {@link DataProvider} interface allowing to retrieve data from a local file. This object can be configured with a {@link JsonObject} that must contain the key 'file' which points to an existing file.
 * @author Andrea Cimmino
 *
 */
public class FileProvider implements DataProvider{

	private static final long serialVersionUID = 1L;
	private File file;
	Logger logger = LoggerFactory.getLogger(FileProvider.class);

	/**
	 * This constructor creates an empty {@link DataProvider} that will need to be configured using a valid {@link JsonObject}
	 */
	public FileProvider() {
		super();
	}

	/**
	 * This constructor instantiates a valid {@link FileProvider} with the provided file
	 * @param file a valid {@link File} that points to the target file
	 */
	public FileProvider(File file) {
		this.file = file;
	}


	public FileProvider(JsonObject arguments) {
		configure(arguments);
	}

	@Override
	public InputStream getData() {
		InputStream stream = null;
		try {
			stream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			logger.error(e.toString());
		}
		return  stream;
	}

	@Override
	public void configure(JsonObject configuration) {
		if(configuration.has("file")) {
			String fileStr = configuration.get("file").getAsString();
			if(fileStr.isEmpty()) {
				throw new IllegalArgumentException("FileProvider needs to receive non empty value for the key 'file'");
			}else{
				this.file = new File(fileStr);
			}
		}else {
			throw new IllegalArgumentException("FileProvider needs to receive json object with the mandatory key 'file'");
		}

	}

}
