package com.taskflow.backend.config;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.taskflow.backend.dto.UserDto;
import com.taskflow.backend.services.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

@RequiredArgsConstructor
@Component
public class UserAuthProvider {

    //Para generar y leer el JWT Token se necesita una clave secreta.
    @Value("${security.jwt.token.secret-key:secret-value}")
    private String secretKey;


    private final UserService userService;


    @PostConstruct
    protected void init() {

        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes()); //Para la creacion de la llave secreta en base 64.
    }


    public String createToken(String email) {

        Date now = new Date();

        Date expiresAt = new Date(now.getTime() + 3_600_000); //Tiempo de expiración del JWT token generado: 1 hora.

        return JWT.create()
                .withIssuer(email)
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .sign(Algorithm.HMAC256(secretKey)); //Firmar el JWT token con la llave secreta creada.
    }


    public Authentication validateToken(String token) {

        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey))
                .build();


        DecodedJWT decodedJWT = verifier.verify(token); //Para verificar la validez del JWT token se decodifica para obtener su valor.
        //Si este excede la fecha de validez, arrojara una exepcion del handler.


        UserDto user = userService.findByLogin(decodedJWT.getIssuer()); //Comprobar si este usuario EXISTE en mi base de datos.

        return new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
    }
}
