package helio.translation;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import helio.Utils;
import helio.blueprints.mappings.Expresions;
import helio.blueprints.mappings.LinkRule;
import helio.blueprints.mappings.TranslationRule;
import helio.blueprints.mappings.TranslationRules;

class VelocityMapper {
	private static final String SUBJECT_VARIABLE = "$subject";
	private static final String SUBJECT_VARIABLE_PREAMBLE = "#set( $subjectSet = \"";
	private static final String PREDICATE_VARIABLE_PREAMBLE = "#set( $predicate";
	private static final String OBJECT_VARIABLE_PREAMBLE = "#set( $object";
	private static final String VARIABLE_POSTAMBLE = "\" )\n";

	private static final String VARIABLE_ASSIGNMENT = "=\"";
	private static final String LITERAL_TOKEN = "$HF.quote()";
	private static final char URI_OPEN_TOKEN = '<';
	private static final String URI_DATATYPE_OPEN_TOKEN = "^^<";
	private static final char URI_CLOSE_TOKEN = '>';
	private static final char KEY_OPEN_TOKEN = '{';
	private static final char KEY_CLOSE_TOKEN = '}';
	private static final char AT_TOKEN = '@';
	private static final String VELOCITY_VARIABLE_OPEN_TOKEN = "${";
	private static final String VELOCITY_FOR_PREAMBLE = " #foreach( ${";
	private static final String VELOCITY_FOR_MIDAMBLE_1 = "} in ${";
	private static final String VELOCITY_FOR_MIDAMBLE_2 = "SET}) ";
	private static final String VELOCITY_FOR_POSTAMBLE = " #end";
	private static final String VELOCITY_FOR_POSTAMBLE_NEWLINE = "#end\n";
	private static final String IF_STATEMENT_PREAMBLE = "#if( $HF.notBlank($subject) && $HF.notBlank($predicate";
	private static final String IF_STATEMENT_MIDAMBLE = ") && $HF.notBlank($object";
	private static final String IF_STATEMENT_POSTAMBLE = 	") )\n";


	// -- Constructor
	private VelocityMapper() {
		super();
	}

	// link rules

	protected static String toVelocityTemplate(TranslationRules rulesSource, TranslationRules rulesTarget, LinkRule rule) {
		StringBuilder triplet = new StringBuilder();
		String graphId = Utils.concatenate("${HF.formatGraphRepository(",SUBJECT_VARIABLE,",\"?links=", Utils.mapTranslationRulesId(rulesSource.getId()), "\")}");
		// Source subject
		List<String> subjectSourceReferences = Expresions.extractDataReferences(rulesSource.getSubject());
		String sourceSubject = VelocityMapper.computeVelocityTemplate4Rule(rulesSource.getSubject(), subjectSourceReferences, null);
		triplet.append("#set( $subjectSourceSet = \"").append(sourceSubject).append(VARIABLE_POSTAMBLE);
		triplet.append("#set($subjectsSource = $HF.splitSubjects($subjectSourceSet) )");
		triplet.append("#foreach( $subject in $subjects )");

		triplet.append("#end");
		return triplet.toString();
	}


	// Translation rules

	protected static String toVelocityTemplate(TranslationRules rules) {
		StringBuilder triplet = new StringBuilder();
		String graphId = Utils.concatenate("${HF.formatGraphRepository(",SUBJECT_VARIABLE,",\"?rule=", Utils.mapTranslationRulesId(rules.getId()), "\")}");
		List<String> subjectReferences = Expresions.extractDataReferences(rules.getSubject());
		String subject = VelocityMapper.computeVelocityTemplate4Rule(rules.getSubject(), subjectReferences, null);

		triplet.append(SUBJECT_VARIABLE_PREAMBLE).append(subject).append(VARIABLE_POSTAMBLE);
		triplet.append("#set($subjects = $HF.splitSubjects($subjectSet) )");
		triplet.append("#foreach( $subject in $subjects )");
		triplet.append("DELETE { GRAPH ").append(graphId).append(" { ?s ?p ?o } }");
		triplet.append("INSERT { GRAPH ").append(graphId).append(" {\n");
		rules.getProperties().stream()
							 .map(VelocityMapper::toVelocityTemplate)
							 .forEach(elem -> triplet.append(elem));
		triplet.append("\n} } WHERE { ?s ?p ?o } ; \n");
		triplet.append("#end");
		//System.out.println(triplet);
		return triplet.toString();
	}






	private static String toVelocityTemplate(TranslationRule rule) {
		StringBuilder builder = new StringBuilder();
		List<String> dataReferencesPredicate =  Expresions.extractDataReferences(rule.getPredicate());
		String predicate = computeVelocityTemplate4Rule(rule.getPredicate(), dataReferencesPredicate, null);

		List<String> dataReferencesObject =  Expresions.extractDataReferences(rule.getObject());
		if(rule.getDataType()!=null)
			dataReferencesObject.addAll(Expresions.extractDataReferences(rule.getDataType()));
		if(rule.getLanguage()!=null)
			dataReferencesObject.addAll(Expresions.extractDataReferences(rule.getLanguage()));

		String object = computeVelocityTemplate4Rule(prepareObjectTemplate(rule), dataReferencesObject, rule);
		String predicateHash =  normalisedHashReference(predicate);
		String objectHash = normalisedHashReference(object);
		builder.append(PREDICATE_VARIABLE_PREAMBLE).append(predicateHash).append(VARIABLE_ASSIGNMENT).append(predicate).append(VARIABLE_POSTAMBLE);
		builder.append(OBJECT_VARIABLE_PREAMBLE).append(objectHash).append(VARIABLE_ASSIGNMENT).append(object).append(VARIABLE_POSTAMBLE);
		builder.append(IF_STATEMENT_PREAMBLE).append(predicateHash).append(IF_STATEMENT_MIDAMBLE).append(objectHash).append(IF_STATEMENT_POSTAMBLE);
		builder.append("$HF.formatNewURI($subject) $HF.formatNewURI($predicate").append(predicateHash).append(") $HF.format($object").append(objectHash).append(",").append(rule.getIsLiteral()).append(",").append(rule.getDataType()!=null).append(",").append(rule.getLanguage()!=null).append(") .\n");

		builder.append(VELOCITY_FOR_POSTAMBLE_NEWLINE);

		return builder.toString();
	}

	private static String prepareObjectTemplate(TranslationRule rule) {
		StringBuilder velocityTemplate  = new StringBuilder();
		if(rule!=null && rule.getIsLiteral()) {
			velocityTemplate.append(LITERAL_TOKEN).append(rule.getObject()).append(LITERAL_TOKEN);
			String datatype = rule.getDataType();
			String lang = rule.getLanguage();
			if(datatype!=null && !datatype.isEmpty())
				velocityTemplate.append(URI_DATATYPE_OPEN_TOKEN).append(datatype).append(URI_CLOSE_TOKEN);
			if(lang!=null && !lang.isEmpty())
				velocityTemplate.append(AT_TOKEN).append(lang);
			if(lang!=null && datatype!=null)
				System.out.println("Somethign wrong in the mappings");
		}else{
			// URIs are formatted latter
			velocityTemplate.append(rule.getObject());
		}
		return velocityTemplate.toString();
	}

	private static String computeVelocityTemplate4Rule(String template, List<String> dataReferences, TranslationRule rule) {
		StringBuilder velocityTemplate = new StringBuilder();
		if(dataReferences.isEmpty()) {
			if(rule==null || (rule!=null &&  !rule.getIsLiteral())) {  // literals are previously formated
				velocityTemplate.append(URI_OPEN_TOKEN).append(template).append(URI_CLOSE_TOKEN);
			}else {
				velocityTemplate.append(template);
			}
		}else {
			// Switch in template the old reference for the normalized one
			String reference = dataReferences.remove(0);
			String normalisedReference = normaliseReference(reference,false);
			String replacedTemplate = StringUtils.replace(template, wrapString(reference,KEY_OPEN_TOKEN,KEY_CLOSE_TOKEN), wrapString(normalisedReference,VELOCITY_VARIABLE_OPEN_TOKEN,KEY_CLOSE_TOKEN));
			velocityTemplate.append(VELOCITY_FOR_PREAMBLE).append(normalisedReference).append(VELOCITY_FOR_MIDAMBLE_1).append(normalisedReference).append(VELOCITY_FOR_MIDAMBLE_2);
			velocityTemplate.append(computeVelocityTemplate4Rule(replacedTemplate, dataReferences, rule)).append(" #if( ($foreach.count) && ($foreach.count <= $").append(normalisedReference).append("SET.size()) ), #end");
			velocityTemplate.append(VELOCITY_FOR_POSTAMBLE);
		}
		return velocityTemplate.toString();
	}


	private static final String VELOCITY_VARIABLE_PREAMBLE = "Hash";
	private static final String VELOCITY_VARIABLE_SET = "SET";
	protected static String normaliseReference(String reference, Boolean isSet) {
		StringBuilder normalisedReference = new StringBuilder();
		String hash = normalisedHashReference(reference);
		normalisedReference.append(VELOCITY_VARIABLE_PREAMBLE);
		normalisedReference.append(hash);
		if(isSet)
			normalisedReference.append(VELOCITY_VARIABLE_SET);
		return normalisedReference.toString();
	}

	protected static String normalisedHashReference(String reference) {
		int hash = reference.hashCode();
		if(hash<0)
			hash = hash*(-1);
		return String.valueOf(hash);
	}


	// Wrapping methods

	private static String wrapString(String string, char head, char tail) {
		StringBuilder str = new StringBuilder();
		str.append(head).append(string).append(tail);
		return str.toString();
	}

	private static String wrapString(String string, String head, char tail) {
		StringBuilder str = new StringBuilder();
		str.append(head).append(string).append(tail);
		return str.toString();
	}
}
