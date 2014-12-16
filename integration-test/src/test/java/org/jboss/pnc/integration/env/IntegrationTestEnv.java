package org.jboss.pnc.integration.env;


/**
 * This is the Util class to get test environment, like the http-port, etc.
 *
 * @author <a href="mailto:lgao@redhat.com">Lin Gao</a>
 *
 *
 */
public final class IntegrationTestEnv {

	private IntegrationTestEnv(){
		// static utils methods only.
	}

	/**
	 * Gets Test Http Port.
	 * 
	 * @return the http port for REST end points, default to 8080.
	 */
	public static int getHttpPort() {
		int defaultHttpPort = Integer.getInteger("jboss.http.port", 8080);
		int offset = Integer.getInteger("jboss.port.offset", 0);
		return defaultHttpPort + offset;
	}

}
