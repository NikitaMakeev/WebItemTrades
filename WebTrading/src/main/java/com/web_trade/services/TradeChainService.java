package com.web_trade.services;

import com.web_trade.entity.Inventory;
import com.web_trade.entity.Item;
import com.web_trade.entity.TradeChain;
import com.web_trade.entity.User;
import com.web_trade.repository.InventoryRepository;
import com.web_trade.repository.TradeChainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class TradeChainService {
    @Autowired
    private final TradeChainRepository tradeChainRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    public TradeChainService(TradeChainRepository tradeChainRepository) {
        this.tradeChainRepository = tradeChainRepository;
    }

    public List<Item> getItemsBelongingToUser(Long tradeChainId, Long userId) {
        Optional<TradeChain> tradeChainOptional = tradeChainRepository.findById(tradeChainId);
        if (tradeChainOptional.isPresent()) {
            TradeChain tradeChain = tradeChainOptional.get();
            if (tradeChain.getUser().getId().equals(userId)) {
                Inventory userInventory = tradeChain.getUser().getInventory();
                if (userInventory != null) {
                    return userInventory.getItems();
                }
            }
        }
        return Collections.emptyList();
    }

    public List<Item> getSenderItems(Long tradeChainId) {
        return getItemsBelongingToUser(tradeChainId, tradeChainRepository.findById(tradeChainId)
                .map(tradeChain -> tradeChain.getSession().getSender().getId())
                .orElse(null));
    }

    public List<Item> getReceiverItems(Long tradeChainId) {
        return getItemsBelongingToUser(tradeChainId, tradeChainRepository.findById(tradeChainId)
                .map(tradeChain -> tradeChain.getSession().getReceiver().getId())
                .orElse(null));
    }
}


