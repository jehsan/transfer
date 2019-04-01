package com.ehsan;

import com.ehsan.controller.FinancialApi;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.File;

public class Start {

    public static void main(String[] args) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir("temp");
        tomcat.setPort(args.length >= 1 ? Integer.valueOf(args[0]) : 8080);

        String docBase = new File(".").getAbsolutePath();

        Context context = tomcat.addContext("/", docBase);

        Tomcat.addServlet(context, "financial-api", resourceConfig());
        context.addServletMappingDecoded("/*", "financial-api");

        tomcat.start();
        tomcat.getServer().await();
    }

    private static ServletContainer resourceConfig() {
        return new ServletContainer(new ResourceConfig(FinancialApi.class, JacksonJsonProvider.class));
    }

}
