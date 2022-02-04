package helio.components.functions;

import java.util.Date;

import org.apache.commons.digester.RegexMatcher;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import helio.blueprints.components.MappingFunctions;

public class HF implements MappingFunctions {

	// RDF generation functions

	private static final String QUOTATION = "\"";
	public static String format(String node, Boolean isLiteral, Boolean hasDatatype, Boolean hasLanguage) {
		node = node.trim();
		if(isLiteral && !hasDatatype && !hasLanguage)
			System.out.println("here");
		if(!isLiteral) {
			return formatNewURI(node);
		}else if(isLiteral && !hasDatatype && !hasLanguage && node.matches("\"[^\"]*\"\s+\"[^\"]*\".*")) {
			return node.replaceAll("\"\s+\"", "\", \"");
		}else if(isLiteral && !hasDatatype && hasLanguage && node.matches("\"[^\"]*\"@[A-z]{2}\s*.+")) {
			return node.replaceAll("\s+\"", ", \"");
		}else if(isLiteral && hasDatatype && !hasLanguage && node.matches("\"[^\"]*\"[^>]+>\s*.+")){
			return node.replaceAll(">\s+\"", ">, \"");
		}else{
			return node;
		}
	}

	private static final String TOKEN_CLOSE_RDF_RESOURCE = ">";
	public static String formatGraphRepository(String str, String replacement) {
		StringBuilder format = new StringBuilder();
		format.append(str.replace(TOKEN_CLOSE_RDF_RESOURCE, replacement).trim()).append(TOKEN_CLOSE_RDF_RESOURCE);
		return formatNewURI(format.toString());
	}

	private static final String TOKEN_SPACE = "\s+";
	private static final String TOKEN_UNDERSCORE = "_";
	private static final String TOKEN_WRONG_SPACE = ">,?[_]+<";
	private static final String TOKEN_FIXED_SPACE = ">, <";
	public static String formatNewURI(String uri) {
		String clean = uri.trim().replaceAll(TOKEN_SPACE, TOKEN_UNDERSCORE).replaceAll(TOKEN_WRONG_SPACE, TOKEN_FIXED_SPACE);
		return clean;
	}

	public static String quote() {
		return QUOTATION;
	}

	public static boolean notBlank(String str) {
		return !str.trim().isEmpty();
	}

	public static String[] splitSubjects(String multisubject) {
		if(multisubject.contains(">,  <"))
			return multisubject.trim().split(",\\s+");
		if(multisubject.matches("[^>]+>\\s+<.+"))
			return multisubject.trim().split("\\s+");
		return new String[] {multisubject.trim()};
	}


	// -- Other functions

	public static String lower(String str) {
		return StringUtils.lowerCase(str);
	}

	public static String trim(String str) {
		return str.trim();
	}


	public static String now() {
		Date date = new Date();
		return date.toString();
	}


	public static String regex_replace(String str, String regex, String replacement) {
		return str.replaceAll(regex, replacement);
	}


}
