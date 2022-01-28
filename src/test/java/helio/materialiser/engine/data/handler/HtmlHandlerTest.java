package helio.materialiser.engine.data.handler;

import java.util.Queue;

import org.junit.Assert;
import org.junit.Test;

import com.google.gson.JsonObject;

import helio.components.handlers.HtmlHandler;
import helio.components.providers.URLProvider;

public class HtmlHandlerTest {



	@Test
	public void htmlTest() throws Exception {
		URLProvider url = new URLProvider("https://scholar.google.es/citations?user=lWFCvMwAAAAJ&hl=es");

		JsonObject configuration = new JsonObject();
		configuration.addProperty("iterator", ".gsc_rsb");


		HtmlHandler html = new HtmlHandler();
		html.configure(configuration);
		Queue<String> dataSplitted = html.splitData(url.getData());
		String dataFragment = dataSplitted.poll();
		Assert.assertTrue(Integer.valueOf(html.filter("tbody > tr:eq(0) > td:eq(1)", dataFragment).get(0)) > 0 );

	}
}
