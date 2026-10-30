package pe.edu.upeu.vetandes.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey clave;

    public JwtService(@Value("${app.jwt.secret}") String secreto) {
        this.clave = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secreto)
        );
    }

    public String generar(String username) {
        long ahora = System.currentTimeMillis();

        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(ahora))
                .expiration(new Date(ahora + 3600000))
                .signWith(clave)
                .compact();
    }

    public String validar(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}