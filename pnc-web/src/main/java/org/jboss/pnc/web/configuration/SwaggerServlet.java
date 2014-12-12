package org.jboss.pnc.web.configuration;

import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.reader.ClassReaders;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

@WebServlet(name = "SwaggerJaxrsConfig", loadOnStartup = 1)
public class SwaggerServlet extends HttpServlet {

    @Override
    public void init(ServletConfig servletConfig) {
        try {
            super.init(servletConfig);
            SwaggerConfig swaggerConfig = new SwaggerConfig();
            ConfigFactory.setConfig(swaggerConfig);
            swaggerConfig.setApiVersion("1.0.0");
            swaggerConfig.setBasePath(getBaseUrl());
            ScannerFactory.setScanner(new DefaultJaxrsScanner());
            ClassReaders.setReader(new DefaultJaxrsApiReader());
        } catch (ServletException | IOException e) {
            throw new IllegalStateException(e);
        }
    }

    String getBaseUrl() throws IOException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("/swagger.properties");
        Properties properties = new Properties();
        try (InputStream inStream = resource.openStream()) {
            properties.load(inStream);
        }
        return properties.getProperty("baseUrl");
    }

}
