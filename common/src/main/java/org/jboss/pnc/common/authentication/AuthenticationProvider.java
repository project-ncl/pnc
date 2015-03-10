package org.jboss.pnc.common.authentication;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;


@Stateless
public class AuthenticationProvider {
    @Resource
    private SessionContext sctx;
    
    public String getLoggedInUser() {
        return sctx.getCallerPrincipal().getName();
      }
}
