package helio.translation;

import java.util.TimerTask;

import helio.bleprints.mappings.TranslationUnit;

/**
 * This class wraps {@link AsyncronousTranslationTask} in order for them to be executed asynchronously.
 * @author Andrea Cimmino
 *
 */
class ScheduledTranslationTask extends TimerTask {

	private TranslationUnit translationUnit;

	/**
	 * This constructor wraps the provided {@link AsyncronousTranslationTask} into an {@link ScheduledTranslationTask} that will be executed asynchronously
	 * @param synchronousTask a {@link AsyncronousTranslationTask} to be executed asynchronously
	 */
	public ScheduledTranslationTask(TranslationUnit translationUnit) {
		this.translationUnit = translationUnit;

	}

	@Override
	public void run() {
		translationUnit.translate();
	}

	public static ScheduledTranslationTask create(TranslationUnit unit) {
		return new ScheduledTranslationTask(unit);
	}

	public TranslationUnit getTranslationUnit() {
		return this.translationUnit;
	}

}
