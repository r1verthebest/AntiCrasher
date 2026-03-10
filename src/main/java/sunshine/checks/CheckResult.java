package core.sunshine.checks;

public abstract class CheckResult {

	private final String reason;
	private final boolean flagged;

	protected CheckResult(boolean flagged, String reason) {
		this.flagged = flagged;
		this.reason = reason != null ? reason : "";
	}

	public boolean isFlagged() {
		return this.flagged;
	}

	public boolean check() {
		return isFlagged();
	}

	public String getReason() {
		return this.reason;
	}

	public static final class Negative extends CheckResult {

		public static final Negative SAFE = new Negative("");

		public Negative(String reason) {
			super(false, reason);
		}

		public Negative() {
			super(false, "");
		}
	}

	public static final class Positive extends CheckResult {
		public Positive(String reason) {
			super(true, reason);
		}

		public Positive() {
			super(true, "");
		}
	}
}