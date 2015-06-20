package citrix.moonshot.enums;

public enum BootTarget {

	NONE("None"), NA("N/A"), PXE("PXE"), HDD("HDD"), ISCSI("iSCSI"), M2("M.2");

	private String name;

	private BootTarget(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
