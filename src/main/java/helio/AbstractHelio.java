package helio;

import org.apache.jena.shared.impl.JenaParameters;

public abstract class AbstractHelio {


	public static boolean isSilentAcceptanceOfUnknownDatatypes() {
		return JenaParameters.enableSilentAcceptanceOfUnknownDatatypes;
	}

	public static void setSilentAcceptanceOfUnknownDatatypes(boolean silentAcceptanceOfUnknownDatatypes) {
		JenaParameters.enableSilentAcceptanceOfUnknownDatatypes=silentAcceptanceOfUnknownDatatypes;
	}

	public static boolean isEagerLiteralValidation() {
		return JenaParameters.enableEagerLiteralValidation;
	}

	public static void setEagerLiteralValidation(boolean eagerLiteralValidation) {
		JenaParameters.enableEagerLiteralValidation = eagerLiteralValidation;
	}

	public static boolean isbNodeUIDGeneration() {
		return JenaParameters.disableBNodeUIDGeneration;
	}

	public static void setbNodeUIDGeneration(boolean bNodeUIDGeneration) {
		JenaParameters.disableBNodeUIDGeneration=bNodeUIDGeneration;
	}

	public static boolean isOwlRuleOverOWLRuleWarnings() {
		return JenaParameters.enableOWLRuleOverOWLRuleWarnings;
	}

	public static void setOwlRuleOverOWLRuleWarnings(boolean owlRuleOverOWLRuleWarnings) {
		JenaParameters.enableOWLRuleOverOWLRuleWarnings=owlRuleOverOWLRuleWarnings;
	}

}
