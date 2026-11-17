package com.library.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@SpringBootApplication(scanBasePackages = "com.library")
public class LibraryWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(LibraryWebApplication.class, args);
    }

    @Bean
    FilterRegistrationBean<WebAuthenticationFilter> webAuthenticationFilter() {
        FilterRegistrationBean<WebAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new WebAuthenticationFilter());
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
