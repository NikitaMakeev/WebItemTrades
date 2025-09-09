package com.web_trade.services;

import com.web_trade.components.AuthenticationFacade;
import com.web_trade.entity.Inventory;
import com.web_trade.entity.Item;
import com.web_trade.entity.User;
import com.web_trade.repository.InventoryRepository;
import com.web_trade.repository.ItemRepository;
import com.web_trade.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ItemService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AuthenticationFacade authenticationFacade;


    public List<Item> getUserItems() {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            User user = currentUser.getUser();

            user = userRepository.findUserWithInventoryAndItems(user.getId());
            Inventory inventory = user.getInventory();
            return inventory.getItems();
        }
        return new ArrayList<>();
    }

    public void deleteItem(Long itemId) {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            User user = currentUser.getUser();

            user = userRepository.findUserWithInventoryAndItems(user.getId());
            Inventory inventory = user.getInventory();

            Item itemToDelete = inventory.getItems().stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);

            if (itemToDelete != null) {

                inventory.getItems().remove(itemToDelete);
                itemRepository.delete(itemToDelete);
            }
        }
    }

    public void addItem(Item item, MultipartFile imageFile) throws IOException {
        String imageName = saveImage(imageFile);
        item.setImageName(imageName);

        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            User user = currentUser.getUser();

            user = userRepository.findUserWithInventoryAndItems(user.getId());

            itemRepository.save(item);

            user.getInventory().getItems().add(item);
            userRepository.save(user);
        }
    }

    private String saveImage(MultipartFile imageFile) throws IOException {
        if (imageFile.isEmpty()) {
            throw new IOException("Failed to store empty file");
        }

        String imageName = imageFile.getOriginalFilename();
        if (imageName == null || imageName.contains("..")) {
            throw new IOException("Cannot store file with relative path outside current directory " + imageName);
        }

        byte[] bytes = imageFile.getBytes();
        Path path = Paths.get("src/main/resources/static/images/").resolve(imageName);

        Files.createDirectories(path.getParent());
        Files.write(path, bytes);

        return imageName;
    }

}
