package org.jboss.pnc.web;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class PNCStartupLogger implements ServletContextListener {

    public static final Logger log = Logger.getLogger(PNCStartupLogger.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent event) {
        log.info ("Starting up PNC " + getManifestInformation());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Shutting down PNC");
    }

    private String getManifestInformation()
    {
        String result = "";
        try
        {
            final Enumeration<URL> resources = PNCStartupLogger.class.getClassLoader()
                                                                         .getResources( "META-INF/MANIFEST.MF" );

            while ( resources.hasMoreElements() )
            {
                final URL jarUrl = resources.nextElement();

                log.fine("Processing jar resource " + jarUrl);
                if ( jarUrl.getFile().contains( "pnc-web" ) )
                {
                    final Manifest manifest = new Manifest( jarUrl.openStream() );
                    result = manifest.getMainAttributes()
                                     .getValue( "Implementation-Version" );
                    result += " ( SHA: " + manifest.getMainAttributes()
                                                   .getValue( "Scm-Revision" ) + " ) ";
                    break;
                }
            }
        }
        catch ( final IOException e )
        {
            log.log(Level.SEVERE, "Error retrieving information from manifest", e);
        }

        return result;
    }
}
