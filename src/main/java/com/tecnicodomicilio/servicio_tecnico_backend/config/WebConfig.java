package com.tecnicodomicilio.servicio_tecnico_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path carpeta = Paths.get("uploads").toAbsolutePath();
        String ubicacion = "file:" + carpeta.toString().replace("\\", "/") + "/";

        registry.addResourceHandler("/uploads/**").addResourceLocations(ubicacion);
    }
}