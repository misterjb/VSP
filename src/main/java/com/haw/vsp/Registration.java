package com.haw.vsp;

public class Registration {
	String heroclass;
	String capabilities;
	String url;
	String id;
	public Registration(String heroclass, String capabilities, String url) {
		this.heroclass = heroclass;
		this.capabilities = capabilities;
		this.url = url;
	}
public void setId(String id) {
	this.id = id;
}public String getId() {
	return id;
}
	public String getCapabilities() {
		return capabilities;
	}

	public String getHeroclass() {
		return heroclass;
	}

	public String getUrl() {
		return url;
	}

	public void setCapabilities(String capabilities) {
		this.capabilities = capabilities;
	}

	public void setHeroclass(String heroclass) {
		this.heroclass = heroclass;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
