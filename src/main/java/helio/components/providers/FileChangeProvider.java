package helio.components.providers;

import java.io.InputStream;

import com.google.gson.JsonObject;

import helio.blueprints.components.AsyncDataProvider;
import helio.blueprints.mappings.TranslationUnit;

public class FileChangeProvider implements AsyncDataProvider{

	TranslationUnit unit;

	@Override
	public void configure(JsonObject configuration) {
		// TODO Auto-generated method stub

	}

	@Override
	public InputStream getData() {
		//InputStream stream = new
		//;
		return null;
	}

	@Override
	public void setTranslationUnit(TranslationUnit translationUnit) {
		unit =  translationUnit;

	}


}
