package org.jboss.pnc.common.authentication;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;


@Stateless
public class AuthenticationProvider {
    @Resource
    private SessionContext sctx;
    private static String loggedInUser;
    
    public String getLoggedInUser() {
        if(loggedInUser == null) {
            loggedInUser =  sctx.getCallerPrincipal().getName();
        }
        return loggedInUser;
      }
}
