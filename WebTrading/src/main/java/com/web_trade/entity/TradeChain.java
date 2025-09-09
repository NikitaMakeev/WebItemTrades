package com.web_trade.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "trade_chain")
public class TradeChain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "session_id")
    private Session session;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany
    private List<Item> items;


}
