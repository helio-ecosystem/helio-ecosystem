package helio;

import helio.blueprints.components.TranslationUnit;

/**
 * This class wraps {@link AsyncronousTranslationTask} in order for them to be executed asynchronously.
 * @author Andrea Cimmino
 *
 */
class SyncronousTranslationTask {

	private TranslationUnit unit;

	/**
	 * This constructor wraps the provided {@link TranslationUnit} into an {@link SyncronousTranslationTask} that will be executed timely with a certain interval
	 * @param synchronousTask a {@link TranslationUnit} to be executed according to a certain interval
	 */
	public SyncronousTranslationTask(TranslationUnit unit) {
		this.unit = unit;
	}


	public void run() {
		unit.translate();
	}

	public static SyncronousTranslationTask create(TranslationUnit unit) {
		return new SyncronousTranslationTask(unit);
	}

	public TranslationUnit getTranslationUnit() {
		return this.unit;
	}
}
