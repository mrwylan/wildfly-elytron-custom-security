package com.trivadis.mze.elytrondemo.ejb;

import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

import com.trivadis.mze.elytrondemo.ejb.remote.UserServiceRemote;

@Stateless
@PermitAll
@Remote(UserServiceRemote.class)
public class UserService implements UserServiceRemote {

    @Resource
    private SessionContext ctx;

	@Override
	public String whoAmI() {
        Principal principal = ctx.getCallerPrincipal();
		return principal.getName();
	}
}
