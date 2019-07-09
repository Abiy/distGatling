package com.alh.gatling.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;

import com.alh.gatling.security.rest.RestAuthenticationAccessDeniedHandler;
import com.alh.gatling.security.rest.RestAuthenticationEntryPoint;
import com.alh.gatling.service.SimpleCORSFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${security.username}") private String USERNAME;
    @Value("${security.password}") private String PASSWORD;
	
    @Override
    public void configure(WebSecurity web) throws Exception {
        // Filters will not get executed for the resources
        web.ignoring().antMatchers("/", "/resources/**", "/static/**", "/public/**", "/webui/**", "/h2-console/**"
            , "/gatling/lib/**", "/configuration/**", "/swagger-ui/**", "/swagger-resources/**", "/api-docs", "/api-docs/**", "/v2/api-docs/**"
            , "/*.html", "/**/*.html" ,"/**/*.css","/**/*.js","/**/*.png","/**/*.jpg", "/**/*.gif", "/**/*.svg", "/**/*.ico", "/**/*.ttf","/**/*.woff","/**/*.otf");
    }

    @Override public void configure(AuthenticationManagerBuilder auth) throws Exception {
    	auth.inMemoryAuthentication()
    	.withUser(USERNAME)
    	.password(PASSWORD)
    	.roles("USER", "ACTUATOR");
    }

    @Override protected void configure(HttpSecurity http) throws Exception {
        http
                .antMatcher("/**").csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .accessDeniedHandler(new RestAuthenticationAccessDeniedHandler())
                .and()
                .authorizeRequests()
                .antMatchers("/gatling/server/abort**", "/gatling/server/getlog/**").permitAll()
                .anyRequest().hasRole("USER")
                .and()
                .addFilterBefore(new SimpleCORSFilter(), ChannelProcessingFilter.class)
                .httpBasic();
    }

}
