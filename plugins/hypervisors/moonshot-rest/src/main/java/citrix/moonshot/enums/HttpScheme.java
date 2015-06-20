package citrix.moonshot.enums;

public enum HttpScheme {
	HTTP("http"), HTTPS("https");

	private String name;

	private HttpScheme(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}