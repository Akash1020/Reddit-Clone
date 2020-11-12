package io.github.anantharajuc.rc.authentication.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.anantharajuc.rc.authentication.model.RefreshToken;
import io.github.anantharajuc.rc.authentication.repository.RefreshTokenRepository;
import io.github.anantharajuc.rc.exceptions.SpringRedditException;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@AllArgsConstructor
@Transactional
@Log4j2
public class RefreshTokenServiceImpl implements RefreshTokenService
{
	private final RefreshTokenRepository refreshTokenRepository;
	
	@Override
	public RefreshToken generateRefreshToken(String username) 
	{
		 RefreshToken refreshToken = new RefreshToken(); 
		 
		 refreshToken.setToken(UUID.randomUUID().toString());
		 refreshToken.setUsername(username); 
		 
		 return refreshTokenRepository.save(refreshToken);
	}

	@Override
	public void validateRefreshToken(String token) 
	{
		refreshTokenRepository.findByToken(token).orElseThrow(() -> new SpringRedditException("Invalid Refresh Token"));
	}

	@Override
	public void deleteByToken(String token, String username) 
	{
		log.info("Delete by token method start");
		
		RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow(() -> new SpringRedditException("Invalid Refresh Token"));
		//Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
		
		//log.info(refreshToken.isPresent());
		//log.info(refreshToken.get().getToken().equals(token));
		//log.info(refreshToken.get().getUsername().equals(username));
		
		
		if(refreshToken.getToken().equals(token) && refreshToken.getUsername().equals(username)) 
		{
			log.info("Delete by token condition satisfied");	
				
			refreshTokenRepository.deleteByToken(token); 
		}
		else
		{
			log.info("Token, Username mismatch!");	
			
			throw new SpringRedditException("Token, Username mismatch!");
		}
	}
}
