package com.web_trade.services;


import com.web_trade.components.AuthenticationFacade;
import com.web_trade.entity.Inventory;
import com.web_trade.entity.User;
import com.web_trade.repository.InventoryRepository;
import com.web_trade.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    public void deleteItemId(Long id) {
        inventoryRepository.deleteById(id);
    }


}
