package io.sequences.samples;

import io.swagger.sample.servlet.SampleServlet;
import io.swagger.sample.util.ApiOriginFilter;
import io.swagger.servlet.config.DefaultServletConfig;
import io.swagger.servlet.listing.ApiDeclarationServlet;

import org.eclipse.jetty.server.Server;

import java.net.URI;
import java.net.URL;
import java.util.EnumSet;
import javax.servlet.DispatcherType;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class EmbeddedServer {

    public static final int SERVER_PORT = 15005;
    public static final String SAMPLE_SERVLET_MAPPING = "/sample/users/*";
    public static final String API_DECLARATION_MAPPING = "/api-docs/*";
    public static final String API_VIEW_MAPPING = "/ui/*";
    public static final String API_CFG_MAPPING = "/cfg";
    public static final String DEFAULT_MAPPING = "/";
    public static final String API_FILTER_MAPPING = "/*";

    public static final String SWAGGER_CFG_PACKAGE_PARAM_NAME = "swagger.resource.package";
    public static final String SWAGGER_CFG_PACKAGE_PARAM_VALUE = "io.swagger.sample.servlet";
    public static final String SWAGGER_CFG_BASE_PARAM_NAME = "swagger.api.basepath";
    public static final String SWAGGER_CFG_BASE_PARAM_VALUE = "http://localhost:15005";
    public static final String SWAGGER_CFG_API_PARAM_NAME = "api.version";
    public static final String SWAGGER_CFG_API_PARAM_VALUE = "1.0.0";
    public static final String SWAGGER_UI_BASE_NAME = "resourceBase";
    public static final String SWAGGER_UI_DIR_NAME = "dirAllowed";
    public static final String SWAGGER_UI_PATH_NAME = "pathInfoOnly";
    public static final String SWAGGER_TRUE_VALUE = "true";
    public static final String SWAGGER_UI_WELCOME_VALUE = "index.html";
    public static final int SWAGGER_CFG_INIT_ORDER = 2;

    public static void main(final String[] args) {

        try {
            // Set up server
            Server webServer = new Server(SERVER_PORT);

            // Set up shutdown handler
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {

                    try {

                        webServer.stop();
                    } catch (Exception error) {

                        error.printStackTrace();
                    }
                }
            });

            // Get JAR resources base URI
            ClassLoader loader = EmbeddedServer.class.getClassLoader();
            URL resourceUrl = loader.getResource("web");
            URI webRootUri = resourceUrl.toURI();
            URI uiURI = loader.getResource("web/ui").toURI();

            // Set up handlers
            ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
            handler.setResourceBase(webRootUri.toString());
            handler.setContextPath(DEFAULT_MAPPING);
            handler.setWelcomeFiles(new String[] {
                SWAGGER_UI_WELCOME_VALUE
            });
            webServer.setHandler(handler);

            // Add sample servlet
            handler.addServlet(SampleServlet.class, SAMPLE_SERVLET_MAPPING);

            // Set up the swagger UI
            ServletHolder swaggerUIHolder = new ServletHolder(DefaultServlet.class);
            swaggerUIHolder.setInitParameter(
                SWAGGER_UI_BASE_NAME,
                uiURI.toString());
            swaggerUIHolder.setInitParameter(
                SWAGGER_UI_DIR_NAME,
                SWAGGER_TRUE_VALUE);
            swaggerUIHolder.setInitParameter(
                SWAGGER_UI_PATH_NAME,
                SWAGGER_TRUE_VALUE);
            handler.addServlet(swaggerUIHolder, API_VIEW_MAPPING);

            // Add swagger generation
            ServletHolder swaggerConfigHolder = new ServletHolder(DefaultServletConfig.class);
            swaggerConfigHolder.setInitParameter(
                SWAGGER_CFG_PACKAGE_PARAM_NAME,
                SWAGGER_CFG_PACKAGE_PARAM_VALUE);
            swaggerConfigHolder.setInitParameter(
                SWAGGER_CFG_BASE_PARAM_NAME,
                SWAGGER_CFG_BASE_PARAM_VALUE);
            swaggerConfigHolder.setInitParameter(
                SWAGGER_CFG_API_PARAM_NAME,
                SWAGGER_CFG_API_PARAM_VALUE);
            swaggerConfigHolder.setInitOrder(SWAGGER_CFG_INIT_ORDER);
            handler.addServlet(swaggerConfigHolder, API_CFG_MAPPING);

            handler.addServlet(ApiDeclarationServlet.class, API_DECLARATION_MAPPING);
            handler.addFilter(ApiOriginFilter.class, API_FILTER_MAPPING, EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC));

            // Add the default handler last
            ServletHolder defaultHolder = new ServletHolder("default", DefaultServlet.class);
            defaultHolder.setInitParameter(
                SWAGGER_UI_DIR_NAME,
                SWAGGER_TRUE_VALUE);
            handler.addServlet(defaultHolder, DEFAULT_MAPPING);

            // Run the server
            webServer.start();
            webServer.join();
        } catch (Exception error) {

            error.printStackTrace();
        }
    }
}
