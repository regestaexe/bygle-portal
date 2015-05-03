package net.bygle.portal.bean;

public class FacetBean {

	private String label, value, alias;
	private int count;

	public void setLabel(String label) {
		this.label = label;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}

	public String getAlias() {
		return alias;
	}

	public int getCount() {
		return count;
	}

}
