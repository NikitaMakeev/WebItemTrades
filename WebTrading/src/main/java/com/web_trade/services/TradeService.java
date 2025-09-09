package com.web_trade.services;

import com.web_trade.components.AuthenticationFacade;
import com.web_trade.entity.*;
import com.web_trade.repository.ItemRepository;
import com.web_trade.repository.SessionRepository;
import com.web_trade.repository.TradeChainRepository;
import com.web_trade.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class TradeService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private TradeChainRepository tradeChainRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private AuthenticationFacade authenticationFacade;


    public void createTrade(Session tradeSession, Long receiverId) {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            User sender = currentUser.getUser();

            Optional<User> receiverOptional = userRepository.findById(receiverId);
            if (receiverOptional.isPresent()) {
                User receiver = receiverOptional.get();

                tradeSession.setSender(sender);
                tradeSession.setReceiver(receiver);
                tradeSession.setStatus(2);

                sessionRepository.save(tradeSession);

                TradeChain tradeChain = new TradeChain();
                tradeChain.setSession(tradeSession);
                tradeChain.setUser(sender);
                tradeChainRepository.save(tradeChain);
            } else {
                throw new NoSuchElementException("Receiver not found with ID: " + receiverId);
            }
        }
    }


    public List<Item> getAvailableItemsForTrade() {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            User user = currentUser.getUser();

            user = userRepository.findUserWithInventoryAndItems(user.getId());
            return user.getInventory().getItems();
        }
        return List.of();
    }

    public void addItemsToTrade(Long tradeId, List<Long> itemIds) {
        TradeChain tradeChain = tradeChainRepository.findBySessionId(tradeId);
        List<Item> items = itemRepository.findAllById(itemIds);

        tradeChain.getItems().addAll(items);
        tradeChainRepository.save(tradeChain);
    }


    public void addItemToTrade(Long itemId) {
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            User sender = currentUser.getUser();

            TradeChain tradeChain = (TradeChain) tradeChainRepository.findByUser(sender);
            if (tradeChain == null) {
                tradeChain = new TradeChain();
                tradeChain.setUser(sender);
                tradeChain = tradeChainRepository.save(tradeChain);
            }

            tradeChain.getItems().add(item);
            tradeChainRepository.save(tradeChain);
        }
    }



}
