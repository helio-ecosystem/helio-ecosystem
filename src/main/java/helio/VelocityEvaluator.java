package helio;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

import helio.blueprints.Components;
import helio.blueprints.mappings.TranslationRules;



class VelocityEvaluator {

	// -- Attributes
	protected static final org.apache.velocity.app.VelocityEngine velocityEngine;
	protected static final StringResourceRepository templateRepository;
	//protected static Map<String, Object> functionClasses = new HashMap<>();
	private static final char SPACE = ' ';
	private static final char DOWN_DASH = '_';
	static {
		velocityEngine = new org.apache.velocity.app.VelocityEngine();
		velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER, "string");
		velocityEngine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
		velocityEngine.addProperty("string.resource.loader.repository.static", "false");
		try {
			velocityEngine.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		templateRepository = (StringResourceRepository) velocityEngine.getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
		
	}


	// -- Constructor

	private VelocityEvaluator() {
		super();
	}

	// -- CRUD over templates

	public static String registerVelocityTemplate(TranslationRules rules) {
		String template = VelocityMapper.toVelocityTemplate(rules);
		String rulesId = rules.getId().replace(SPACE, DOWN_DASH);
		VelocityEvaluator.registerVelocityTemplate(rulesId, template);
		templateRepository.putStringResource(rulesId, template);
		return rulesId;
	}

	// TODO: remove this?
	public static void registerVelocityTemplate(String templateId, String velocityTemplate) {
		templateRepository.putStringResource(templateId, velocityTemplate);
	}

	public static void removeVelocityTemplate(String templateId) {
		templateRepository.removeStringResource(templateId);
	}

	public static boolean existsVelocityTemplate(String templateId) {
		return getVelocityTemplate(templateId)!=null;
	}

	public static Template getVelocityTemplate(String templateId) {
		try {
			return velocityEngine.getTemplate(templateId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// -- Evaluation over templates

	public static StringWriter evaluateTemplate(String templateId, Map<String, List<String>> dataReferences)  {
		VelocityContext context = new VelocityContext();
		// Load functions
		Components.getMappingFunctions().entrySet().parallelStream().forEach(entry -> context.put(entry.getKey(), entry.getValue()));

		// Load references
		dataReferences.entrySet().parallelStream().forEach(entry -> context.put(VelocityMapper.normaliseReference(entry.getKey(), true), entry.getValue()));
		// Solve template
		StringWriter writer = new StringWriter();
		Template template = getVelocityTemplate(templateId);
		try {
			template.merge( context, writer );
		} catch (Exception e) {
			e.printStackTrace();
		}
		return writer;
	}

	public static StringWriter evaluateTemplate(String templateId, String jsonPayload) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, IOException {
		@SuppressWarnings("unchecked")
		Map<String, List<String>> simplifiedMatrix = Utils.GSON.fromJson(jsonPayload, new HashMap<String, List<String>>().getClass());
		return evaluateTemplate(templateId, simplifiedMatrix);
	}

}
