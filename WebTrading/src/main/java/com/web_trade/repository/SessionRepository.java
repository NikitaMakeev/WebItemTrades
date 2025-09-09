package com.web_trade.repository;

import com.web_trade.entity.Inventory;
import com.web_trade.entity.Session;
import com.web_trade.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findBySender(User sender);
    List<Session> findByReceiver(User receiver);

    @Query("SELECT s FROM Session s WHERE (s.sender.id = :userId1 AND s.receiver.id = :userId2) OR (s.sender.id = :userId2 AND s.receiver.id = :userId1)")
    List<Session> findExistingSessionBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}