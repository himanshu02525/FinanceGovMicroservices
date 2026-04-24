package com.finance.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.finance.entity.Token;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    // Used by the JwtFilter to see if a token exists in our records
    Optional<Token> findByToken(String token);

    // Find all "living" tokens for a specific user
    // so we can revoke them all at once.
    @Query("""
      select t from Token t inner join User u on t.user.id = u.id
      where u.id = :userId and (t.expired = false or t.revoked = false)
      """)
    List<Token> findAllValidTokensByUser(Long userId);
}