package io.github.anantharajuc.rc.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import io.github.anantharajuc.rc.exceptions.SpringRedditException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import static io.jsonwebtoken.Jwts.parser;

@Service
public class JwtProvider 
{
	@Value("${keystore.password}")
	private String keystorePassword;
	
	@Value("${keystore.file.name}")
	private String keystoreFileName;
	
	@Value("${keystore.alias}")
	private String keystoreAlias;
	
	private KeyStore keyStore;

	@PostConstruct
    public void init() 
	{
        try 
        {
            keyStore = KeyStore.getInstance("JKS");
            InputStream resourceAsStream = getClass().getResourceAsStream("/"+keystoreFileName);
            keyStore.load(resourceAsStream, keystorePassword.toCharArray());
        } 
        catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) 
        {
            throw new SpringRedditException("Exception occurred while loading keystore", e);
        }
    }

	private PrivateKey getPrivateKey() 
	{
        try 
        {
            return (PrivateKey) keyStore.getKey(keystoreAlias, keystorePassword.toCharArray());
        } 
        catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) 
        {
            throw new SpringRedditException("Exception occured while retrieving public key from keystore", e);
        }
    }
	
	private PublicKey getPublickey() 
	{
        try 
        {
            return keyStore.getCertificate(keystoreAlias).getPublicKey();
        } 
        catch (KeyStoreException e) 
        {
            throw new SpringRedditException("Exception occured while retrieving public key from keystore", e);
        }
    }
	
	public String generateToken(Authentication authentication)
	{
		User principal = (User) authentication.getPrincipal();
		
		return Jwts.builder()
                .setSubject(principal.getUsername())
                .signWith(getPrivateKey())
                .compact();
	}
	
	public boolean validateToken(String jwt) 
	{
        parser().setSigningKey(getPublickey()).parseClaimsJws(jwt);
        
        return true;
    }
	
	public String getUsernameFromJwt(String token) 
	{
        Claims claims = parser()
			                .setSigningKey(getPublickey())
			                .parseClaimsJws(token)
			                .getBody();

        return claims.getSubject();
    }
}