package citrix.moonshot.enums;

public enum ResetType {

	ON("On"), OFF("Off"), RESET("Reset"), COLD_RESET("ColdReset"), GRACEFUL_SHUTDOWN(
			"GracefulShutdown");

	private String name;

	private ResetType(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}