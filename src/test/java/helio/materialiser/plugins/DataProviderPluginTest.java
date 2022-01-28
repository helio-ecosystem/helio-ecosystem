package helio.materialiser.plugins;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import helio.blueprints.components.DataProvider;

public class DataProviderPluginTest {



	@Test
	public void testPlugableProvider() throws Exception {
	  	ExtensionLoader<DataProvider> loader = new ExtensionLoader<>();
	  	DataProvider somePlugin = loader.loadClass("./src/test/resources/plugins/plugin-test-0.1.0.jar", "helio.plugins.data.providers.PythonCodeProvider", DataProvider.class);
	  	Assert.assertTrue(transformToString(somePlugin.getData()).equals("{ \"key\": \"Ok\", \"number\": 32, \"text\" : \"Lorem ipsum dolor sit amet, consectetur adipiscing elit\" }"));

	}

	@Test
	public void testPlugableProviderFromMapping() throws ClassNotFoundException {
		JsonObject plugin = new JsonObject();
		plugin.addProperty("source", "./src/test/resources/plugins/plugin-test-0.1.0.jar");
		plugin.addProperty("type", "DataProvider");
		plugin.addProperty("class", "helio.plugins.data.providers.PythonCodeProvider");

		JsonArray array = new JsonArray();
		array.add(plugin);
		JsonObject pluginConfiguration = new JsonObject();
		pluginConfiguration.add("plugins", array);

	  	Plugins.loadPluginsFromJsonConfiguration(pluginConfiguration);
		DataProvider provider = Plugins.buildDataProviderByName("PythonCodeProvider");

	  	Assert.assertTrue(transformToString(provider.getData()).equals("{ \"key\": \"Ok\", \"number\": 32, \"text\" : \"Lorem ipsum dolor sit amet, consectetur adipiscing elit\" }"));
	}

	 private String transformToString(InputStream  input) {
			StringBuilder translatedStream = new StringBuilder();
			try {
				 int data = input.read();
				 while(data != -1){
						translatedStream.append((char) data);
			            data = input.read();

			        }
					input.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {


			}
			return translatedStream.toString();
		}


	@Test
	public void testPlugableProviderOnline() throws ClassNotFoundException, FileNotFoundException {

		Gson gson = new Gson();
		JsonReader reader = new JsonReader(
				new FileReader("./src/test/resources/plugins/online/bashprovider-config.json"));
		JsonObject pluginConfiguration = gson.fromJson(reader, JsonObject.class);
		Plugins.loadPluginsFromJsonConfiguration(pluginConfiguration);
		// Configure the plugin
		JsonArray commands = new JsonArray();
		commands.add("bash ./src/test/resources/plugins/online/test.sh");
		JsonObject configure = new JsonObject();
		configure.addProperty("output", "./src/test/resources/plugins/online/test-data.txt");
		configure.add("commands", commands);
		// Create and configure the provider
		DataProvider onlinePlugin = Plugins.buildDataProviderByName("BashProvider");
		onlinePlugin.configure(configure);
		//
		String output = transformToString(onlinePlugin.getData());
		Assert.assertTrue(output.equals("this is working"));
	}


		@Test
		public void testProviderPlugingAndConfigOnline() throws Exception {



			// Read online configuration
			URL url = new URL("https://github.com/oeg-upm/helio-plugins/releases/download/%230.1.5/bashprovider-config.json");
			URLConnection connection = url.openConnection();
			InputStream is = connection.getInputStream();
			String strPluginCopnfig = transformToString(is);
			Gson g = new Gson();
			JsonObject pluginConfig = g.fromJson(strPluginCopnfig, JsonObject.class);
			Plugins.loadPluginsFromJsonConfiguration(pluginConfig);
			// Configure the plugin
			JsonArray commands = new JsonArray();
			commands.add("bash ./src/test/resources/plugins/online/test.sh");
			JsonObject configure = new JsonObject();
			configure.addProperty("output", "./src/test/resources/plugins/online/test-data.txt");
			configure.add("commands", commands);
			// Create and configure the provider
			DataProvider onlinePlugin = Plugins.buildDataProviderByName("BashProvider");
			onlinePlugin.configure(configure);
			//
			String output = transformToString(onlinePlugin.getData());
			Assert.assertTrue(output.equals("this is working"));
		}


}
