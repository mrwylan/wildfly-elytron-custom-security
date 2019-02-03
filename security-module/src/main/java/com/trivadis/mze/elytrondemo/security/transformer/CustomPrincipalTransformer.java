package com.trivadis.mze.elytrondemo.security.transformer;

import java.security.Principal;
import java.util.Map;

import org.wildfly.extension.elytron.Configurable;
import org.wildfly.extension.elytron.capabilities.PrincipalTransformer;

import com.trivadis.mze.elytrondemo.security.common.CustomPrincipal;

/**
 * Transform a {@link Principal} into a {@link CustomPrincipal}.
 * <p/>
 * The principal's name is split into a name and an application name part by the
 * colon character divider.
 * 
 * @author mzehnder
 */
public class CustomPrincipalTransformer implements PrincipalTransformer, Configurable {
	@Override
	public Principal apply(Principal p) {
		String name = p.getName();
		int dividerIndex = name.indexOf(":");
		if (dividerIndex > 0) {
			return new CustomPrincipal(name.substring(0, dividerIndex), name.substring(dividerIndex + 1));
		}

		return new CustomPrincipal(name);
	}

	@Override
	public void initialize(Map<String, String> configuration) {
		if (configuration.containsKey("throwIllegalStateException")) {
			throw new IllegalStateException("Test exception in PrincipalTransformer");
		}
	}
}
