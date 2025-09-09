package com.web_trade.controllers;

import com.web_trade.components.AuthenticationFacade;
import com.web_trade.entity.Item;
import com.web_trade.entity.User;
import com.web_trade.repository.InventoryRepository;
import com.web_trade.repository.ItemRepository;
import com.web_trade.repository.UserRepository;
import com.web_trade.services.CustomUserDetails;
import com.web_trade.services.InventoryService;
import com.web_trade.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Controller
public class ItemController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private AuthenticationFacade authenticationFacade;

    @GetMapping("/create_product")
    public String showCreateProductForm(Model model) {
        model.addAttribute("item", new Item());

        List<Item> items = itemService.getUserItems();
        model.addAttribute("items", items);

        return "product_form";
    }

    @PostMapping("/deleteItem/{itemId}")
    public String deleteItemInCurrentInventory(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return "redirect:/create_product";
    }

    @PostMapping("/process_create_product")
    public String processCreateProduct(@ModelAttribute Item item, @RequestParam("imageFile") MultipartFile imageFile) {
        try {
            itemService.addItem(item, imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            return "error";
        }
        return "redirect:/create_product";
    }

}
