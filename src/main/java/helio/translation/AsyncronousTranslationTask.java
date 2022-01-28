package helio.translation;

import helio.bleprints.mappings.TranslationUnit;

/**
 * This class wraps {@link AsyncronousTranslationTask} in order for them to be executed asynchronously.
 * @author Andrea Cimmino
 *
 */
class AsyncronousTranslationTask {

	private TranslationUnit unit;

	/**
	 * This constructor wraps the provided {@link TranslationUnit} into an {@link AsyncronousTranslationTask} that will be executed timely with a certain interval
	 * @param synchronousTask a {@link TranslationUnit} to be executed according to a certain interval
	 */
	public AsyncronousTranslationTask(TranslationUnit unit) {
		this.unit = unit;
	}


	public void run() {
		unit.translate();
	}

	public static AsyncronousTranslationTask create(TranslationUnit unit) {
		return new AsyncronousTranslationTask(unit);
	}

	public TranslationUnit getTranslationUnit() {
		return this.unit;
	}
}
