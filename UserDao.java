package com.zstu.math.dao;

import com.zstu.math.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByNickname(String nickname);
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);

    @Query("SELECT u FROM User u WHERE u.username = :loginId OR u.nickname = :loginId")
    Optional<User> findByUsernameOrNickname(@Param("loginId") String loginId);
}