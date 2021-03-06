package jwt.auth.security.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import jwt.auth.models.User;
import jwt.auth.security.constants.SecurityConstant;
import jwt.auth.security.model.SecurityUser;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import static jwt.auth.security.constants.SecurityConstant.*;

public class LoginJWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private AuthenticationManager authenticationManager;

    public LoginJWTAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        // sets filter only for this URL
        this.setFilterProcessesUrl(LOGIN_URL);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("JWTAuthenticationFilter: attemptAuthentication() processing "+request.getRequestURI());
        // request body for /login
        // {
        // username:"",
        // password:""
        // }
        //JACKSON ObjectMapper class coverts JSON to object
        try {
            User user = new ObjectMapper().readValue(request.getInputStream(), User.class);
            System.out.println("user: " + user);
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            user.getPassword(),
                            new ArrayList<>()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication auth) throws IOException, ServletException {
        System.out.println("JWTAuthenticationFilter: successfulAuthentication() processing "+request.getRequestURI());
        //create a JWT token
        System.out.println(auth.getPrincipal());
        String username = ((SecurityUser) auth.getPrincipal()).getUsername();

        String token = JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(Algorithm.HMAC512(SECRET.getBytes()));

        //write the token in the response header
        response.addHeader(HEADER_STRING,token);

        //same as new ResponseEntity("Successfully authenticated",HttpStatus.OK);
        response.getWriter().write("Successfully authenticated!");
        response.getWriter().flush();
    }
}
