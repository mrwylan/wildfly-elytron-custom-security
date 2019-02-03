package com.trivadis.mze.elytrondemo.security.realm;

import java.security.Principal;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.wildfly.extension.elytron.Configurable;
import org.wildfly.security.auth.SupportLevel;
import org.wildfly.security.auth.realm.CacheableSecurityRealm;
import org.wildfly.security.auth.server.RealmIdentity;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.auth.server.SecurityRealm;
import org.wildfly.security.authz.Attributes;
import org.wildfly.security.authz.AuthorizationIdentity;
import org.wildfly.security.authz.MapAttributes;
import org.wildfly.security.credential.Credential;
import org.wildfly.security.evidence.Evidence;
import org.wildfly.security.evidence.PasswordGuessEvidence;

/**
 * PoC: a very simple custom {@link SecurityRealm} with configurable authorized
 * users and groups.
 * 
 * @author mzehnder
 */
public class CustomSecurityRealm implements CacheableSecurityRealm, Configurable {

	private Map<String, String> userPwdMap = new HashMap<>();
	private Map<String, Set<String>> userGroupMap = new HashMap<>();

	@Override
	public void initialize(Map<String, String> configuration) {
		for (String key : configuration.keySet()) {
			if (key.startsWith("user.")) {
				userPwdMap.put(key.substring(5), configuration.get(key));
			}
			if (key.startsWith("groups.")) {
				userGroupMap.put(key.substring(7), new HashSet<>(Arrays.asList(configuration.get(key).split(","))));
			}
		}
		System.out.println("CustomSecurityRealm initialized, configured users: " + userPwdMap.keySet() + " Groups: "
				+ userGroupMap);
	}

	@Override
	public void registerIdentityChangeListener(Consumer<Principal> consumer) {
		// nothing required, credentials are static
	}

	@Override
	public SupportLevel getCredentialAcquireSupport(Class<? extends Credential> credentialType, String algorithmName,
			AlgorithmParameterSpec parameterSpec) throws RealmUnavailableException {
		return SupportLevel.UNSUPPORTED;
	}

	// this realm will be able to verify password evidences only
	@Override
	public SupportLevel getEvidenceVerifySupport(Class<? extends Evidence> evidenceType, String algorithmName)
			throws RealmUnavailableException {
		return PasswordGuessEvidence.class.isAssignableFrom(evidenceType) ? SupportLevel.POSSIBLY_SUPPORTED
				: SupportLevel.UNSUPPORTED;
	}

	@Override
	public RealmIdentity getRealmIdentity(final Principal principal) throws RealmUnavailableException {

		if (userPwdMap.containsKey(principal.getName())) {
			System.out.println("getRealmIdentity for: " + principal.getName());

			return new RealmIdentity() {
				@Override
				public Principal getRealmIdentityPrincipal() {
					// Returning a different Principal instance doesn't have any affect!
					// That's why wee need custom
					// org.wildfly.extension.elytron.capabilities.PrincipalTransformer
					// implementations...
					//
					// return new CompositePrincipal(principal, new NumericPrincipal(42l));
					return principal;
				}

				@Override
				public SupportLevel getCredentialAcquireSupport(Class<? extends Credential> credentialType,
						String algorithmName, AlgorithmParameterSpec parameterSpec) throws RealmUnavailableException {
					return SupportLevel.UNSUPPORTED;
				}

				@Override
				public <C extends Credential> C getCredential(Class<C> credentialType)
						throws RealmUnavailableException {
					return null;
				}

				@Override
				public SupportLevel getEvidenceVerifySupport(Class<? extends Evidence> evidenceType,
						String algorithmName) throws RealmUnavailableException {
					return PasswordGuessEvidence.class.isAssignableFrom(evidenceType) ? SupportLevel.SUPPORTED
							: SupportLevel.UNSUPPORTED;
				}

				@Override
				public boolean verifyEvidence(Evidence evidence) throws RealmUnavailableException {
					if (!userPwdMap.containsKey(principal.getName())) {
						return false;
					}

					if (evidence instanceof PasswordGuessEvidence) {
						PasswordGuessEvidence guess = (PasswordGuessEvidence) evidence;
						try {
							return Arrays.equals(userPwdMap.get(principal.getName()).toCharArray(), guess.getGuess());

						} finally {
							guess.destroy();
						}
					}

					return false;
				}

				@Override
				public boolean exists() throws RealmUnavailableException {
					return true;
				}

				@Override
				public Attributes getAttributes() throws RealmUnavailableException {
					Set<String> roles = userGroupMap.get(principal.getName());
					if (roles == null || roles.isEmpty()) {
						System.out.println("Principal " + principal.getName() + " has no groups assigned");
						return Attributes.EMPTY;
					}
					MapAttributes map = new MapAttributes();
					map.addAll("Roles", roles);
					return map;
				}

				@Override
				public AuthorizationIdentity getAuthorizationIdentity() throws RealmUnavailableException {
					return AuthorizationIdentity.basicIdentity(getAttributes());
				}

			};
		}

		System.out.println("Unknown principal: " + principal.getName());

		return RealmIdentity.NON_EXISTENT;
	}

}
