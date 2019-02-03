package com.trivadis.mze.elytrondemo.security.common;

import java.io.Serializable;
import java.security.Principal;

public class CustomPrincipal implements Principal, Serializable {
	private static final long serialVersionUID = 1L;

	private final String name;
	private final String application;

    public CustomPrincipal(String name) {
        this(name, null);
    }

    public CustomPrincipal(String name, String application) {
        this.name = name;
        this.application = application == null ? "unknown" : application;
    }

    @Override
    public String getName() {
    	return name;
    }

    public String getApplication() {
        return application;
    }

	@Override
	public String toString() {
		return "CustomPrincipal [name=" + getName() + ", application=" + application + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((application == null) ? 0 : application.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CustomPrincipal other = (CustomPrincipal) obj;
		if (application == null) {
			if (other.application != null)
				return false;
		} else if (!application.equals(other.application))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
