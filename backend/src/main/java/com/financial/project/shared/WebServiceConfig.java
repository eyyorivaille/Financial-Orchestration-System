package com.financial.project.shared;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Contract-first SOAP wiring for the fake "legacy core banking" settlement service
 * (settlement.internal.soap) - the standard Spring-WS setup (gs-soap-service),
 * serving settlement.xsd's WSDL at /ws/settlement.wsdl.
 */
@EnableWs
@Configuration
class WebServiceConfig {

    @Bean
    ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean
    XsdSchema settlementSchema() {
        return new SimpleXsdSchema(new ClassPathResource("settlement.xsd"));
    }

    @Bean
    DefaultWsdl11Definition settlement(XsdSchema settlementSchema) {
        DefaultWsdl11Definition definition = new DefaultWsdl11Definition();
        definition.setPortTypeName("SettlementPort");
        definition.setLocationUri("/ws");
        definition.setTargetNamespace("http://financial.project.com/settlement");
        definition.setSchema(settlementSchema);
        return definition;
    }
}
