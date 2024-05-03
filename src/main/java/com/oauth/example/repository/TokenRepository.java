package com.oauth.example.repository;

import com.oauth.example.domain.entity.Token;
import com.oauth.example.domain.entity.User;
import com.oauth.example.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends BaseRepository<Token, UUID> {
    @Transactional
    @Modifying
    @Query("update Token t set t.revoked = ?1, t.expired = ?2 where t.user = ?3")
    void updateRevokedAndExpiredByUser(boolean revoked, boolean expired, User user);

//    @Query(value = """
//      select t from Token t inner join User u\s
//      on t.user.id = u.id\s
//      where u.id = :id and (t.expired = false or t.revoked = false)\s
//      """)
//    List<Token> findAllValidTokenByUser(UUID id);

    Optional<Token> findByToken(String token);
}