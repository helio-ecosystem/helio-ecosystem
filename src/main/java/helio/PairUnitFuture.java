package helio;

import java.util.Objects;
import java.util.concurrent.Future;

import helio.blueprints.TranslationUnit;

class PairUnitFuture {

	private TranslationUnit unit;
	private Future<?> future;
	
	
	public PairUnitFuture(TranslationUnit unit, Future<?> future) {
		super();
		this.unit = unit;
		this.future = future;
	}
	public TranslationUnit getUnit() {
		return unit;
	}
	public void setUnit(TranslationUnit unit) {
		this.unit = unit;
	}
	public Future<?> getFuture() {
		return future;
	}
	public void setFuture(Future<?> future) {
		this.future = future;
	}
	
	
	@Override
	public int hashCode() {
		return Objects.hash(unit.getId());
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PairUnitFuture other = (PairUnitFuture) obj;
		return Objects.equals(this.getUnit().getId(), other.getUnit().getId());
	}
	
	
}
