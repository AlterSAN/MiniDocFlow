package MiniDFlow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {



    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.formLogin().defaultSuccessUrl("/doc/main",true);
        http.csrf().disable();
        http.authorizeRequests()
                .mvcMatchers("/").permitAll()
                .mvcMatchers("/doc/main/**").permitAll()
                .mvcMatchers("/reg/**").permitAll()
                .mvcMatchers("/resources/**").permitAll()
                .mvcMatchers("/admin**").hasRole("ADMIN")
                .anyRequest().authenticated();
    }
}