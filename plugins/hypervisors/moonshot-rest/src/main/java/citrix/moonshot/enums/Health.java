package citrix.moonshot.enums;

public enum Health {

	OK("OK"), Warning("Warning"), Critical("Critical");

	private String name;

	private Health(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}