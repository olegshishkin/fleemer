package com.fleemer.config;

import com.fleemer.interceptors.FleemerSessionInitializationInterceptor;
import java.nio.charset.StandardCharsets;
import org.apache.catalina.filters.RemoteIpFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class AppConfig implements WebMvcConfigurer {
    private final FleemerSessionInitializationInterceptor fleemerSessionInitializationInterceptor;

    @Autowired
    public AppConfig(FleemerSessionInitializationInterceptor fleemerSessionInitializationInterceptor) {
        this.fleemerSessionInitializationInterceptor = fleemerSessionInitializationInterceptor;
    }

    /**
     * Is is necessary for 'logback-mongodb-access' artifact.
     * Internally RemoteIpFilter integrates X-Forwarded-For and X-Forwarded-Proto HTTP headers.
     * It is necessary for substitute remote client ip while http-server is proxying all the requests (otherwise all
     * clients has localhost ip address).
     * WARNING: It is used only for Tomcat-server.
     */
    @Bean
    public RemoteIpFilter remoteIpFilter() {
        return new RemoteIpFilter();
    }

    @Bean
    public ClassLoaderTemplateResolver htmlTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCacheable(false);
        return resolver;
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new SessionLocaleResolver();
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        return new LocaleChangeInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
        registry.addInterceptor(fleemerSessionInitializationInterceptor);
    }
}
