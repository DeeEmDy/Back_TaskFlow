package com.taskflow.backend.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Arrays;


//------------------------Clase para configurar el CORS del backend para que acepte solicitudes del FrontEnd-React.
//Ya que por defecto este viene configurado para solo aceptar solicitudes de él mismo
@Configuration
@EnableWebMvc
public class WebConfig {

    @Bean
    public FilterRegistrationBean corsFilter (){

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); //Aceptar todas las credenciales.
        config.addAllowedOrigin("http://localhost:3000/"); //Permisos para la URL del servidor del FrontEnd-React
        //Configuración de datos que contendrá el header de la solicitud HTTP, en ella se envía el JWT Token.
        config.setAllowedHeaders(Arrays.asList(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT
        ));
        //Autorización de solicitudes HTTP tipo: CRUD para el frontend.
        config.setAllowedMethods(Arrays.asList(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name()
        ));
        config.setMaxAge(3600L); //Tiempo asignado en segundos para la aceptación de solicitudes = 30 minutos.
        source.registerCorsConfiguration("/**", config); //Esto se aplica para todas las solicitudes HTTP.
        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(-102); /* Seteo de la posición de esta configuración del Bean en la parte más baja para
        que sea utilizado antes de cualquier filtro de Spring Security - Sería el primero en ejecutarse.
        */
        return bean;
    }
}
