package helio;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import helio.blueprints.objects.TranslationUnit;


/**
 * This class wraps {@link AsyncronousTranslationTask} in order for them to be executed asynchronously.
 * @author Andrea Cimmino
 *
 */
class ScheduledTranslationTask extends TimerTask {

	private static final Timer timer = new Timer();
	private TranslationUnit translationUnit;

	/**
	 * This constructor wraps the provided {@link AsyncronousTranslationTask} into an {@link ScheduledTranslationTask} that will be executed asynchronously
	 * @param synchronousTask a {@link AsyncronousTranslationTask} to be executed asynchronously
	 */
	public ScheduledTranslationTask(TranslationUnit translationUnit, long time) {
		this.translationUnit = translationUnit;
		timer.scheduleAtFixedRate(this, new Date(), time);
	}

	@Override
	public void run() {
		translationUnit.translate();
	}

	public static ScheduledTranslationTask create(TranslationUnit unit, long time) {
		return new ScheduledTranslationTask(unit, time);
	}

	public TranslationUnit getTranslationUnit() {
		return this.translationUnit;
	}

}
