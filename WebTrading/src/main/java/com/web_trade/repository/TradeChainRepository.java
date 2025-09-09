package com.web_trade.repository;

import com.web_trade.entity.TradeChain;
import com.web_trade.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeChainRepository extends JpaRepository<TradeChain, Long> {

    @Query("SELECT tc FROM TradeChain tc WHERE tc.session.id = :sessionId")
    TradeChain findBySessionId(@Param("sessionId") Long sessionId);

    TradeChain findByUser(User user);
}
