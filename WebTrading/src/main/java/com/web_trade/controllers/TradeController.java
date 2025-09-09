package com.web_trade.controllers;

import com.web_trade.components.AuthenticationFacade;
import com.web_trade.entity.Item;
import com.web_trade.entity.Session;
import com.web_trade.entity.TradeChain;
import com.web_trade.entity.Inventory;
import com.web_trade.entity.User;
import com.web_trade.repository.ItemRepository;
import com.web_trade.repository.SessionRepository;
import com.web_trade.repository.TradeChainRepository;
import com.web_trade.repository.UserRepository;
import com.web_trade.services.CustomUserDetails;
import com.web_trade.services.TradeChainService;
import com.web_trade.services.TradeService;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TradeController {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private AuthenticationFacade authenticationFacade;


    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private TradeChainRepository tradeChainRepository;

    @Autowired
    private TradeChainService tradeChainService;


    @GetMapping("/create_trade")
    public String showCreateTradePage(@RequestParam(required = false) String search, Model model) {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            User sender = currentUser.getUser();
            List<User> users;
            if (search != null && !search.isEmpty()) {
                users = userRepository.findByUsernameContainingAndIdNot(search, sender.getId());
            } else {
                users = userRepository.findByIdNot(sender.getId());
            }

            List<Session> sentTrades = sessionRepository.findBySender(sender);
            List<User> usersToRemove = sentTrades.stream().map(Session::getReceiver).collect(Collectors.toList());
            users.removeAll(usersToRemove);
            model.addAttribute("users", users);
            model.addAttribute("search", search);
        }
        return "create_trade";
    }

    @PostMapping("/send_trade")
    public String createTrade(Session tradeSession, @RequestParam Long receiverId) {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            User sender = currentUser.getUser();

            if (sender.getId().equals(receiverId)) {
                return "redirect:/create_trade?error=self_trade";
            }

            User receiver = userRepository.findById(receiverId).orElseThrow();

            tradeSession.setSender(sender);
            tradeSession.setReceiver(receiver);
            tradeSession.setStatus(2); // Created status

            sessionRepository.save(tradeSession);

            TradeChain tradeChain = new TradeChain();
            tradeChain.setSession(tradeSession);
            tradeChain.setUser(sender);
            tradeChainRepository.save(tradeChain);

        }
        return "redirect:/create_trade";
    }

    @GetMapping("/sent_trades")
    public String showSentTrades(Model model) {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            User sender = currentUser.getUser();
            List<Session> trades = sessionRepository.findBySender(sender);
            model.addAttribute("trades", trades);
        }
        return "sent_trades";
    }

    @GetMapping("/sent_trade")
    public String viewSentTrade(@RequestParam("tradeChainId") Long tradeChainId, Model model) {
        Optional<TradeChain> tradeChainOptional = tradeChainRepository.findById(tradeChainId);
        if (tradeChainOptional.isPresent()) {
            TradeChain tradeChain = tradeChainOptional.get();

            Session session = tradeChain.getSession();
            if (session != null) {
                User sender = session.getSender();
                User receiver = session.getReceiver();

                List<Item> senderItems = new ArrayList<>();

                for (Item item : tradeChain.getItems()) {
                    if (sender != null && sender.getInventory() != null && sender.getInventory().getItems().contains(item)) {
                        senderItems.add(item);
                    }
                }

                List<Item> receiverItems = new ArrayList<>();
                for (Item item : tradeChain.getItems()) {
                    if (receiver != null && receiver.getInventory() != null && receiver.getInventory().getItems().contains(item)) {
                        receiverItems.add(item);
                    }
                }

                model.addAttribute("senderItems", senderItems);
                model.addAttribute("receiverItems", receiverItems);
            }
        }

        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            User sender = currentUser.getUser();
            List<Session> trades = sessionRepository.findBySender(sender);
            model.addAttribute("trades", trades);
        }

        return "sent_trade";
    }


    @PostMapping("/cancel_trade")
    public String cancelTrade(@RequestParam Long tradeId) {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            User sender = currentUser.getUser();
            Session session = sessionRepository.findById(tradeId).orElseThrow();

            TradeChain tradeChain = tradeChainRepository.findBySessionId(tradeId);
            if (tradeChain != null) {
                tradeChainRepository.delete(tradeChain);
            }

            sessionRepository.deleteById(tradeId);

            return "redirect:/sent_trades";
        }
        return "redirect:/create_trade";
    }

    @PostMapping("/accept_trade")
    public String acceptTrade(@RequestParam("tradeChainId") Long tradeChainId) {
        Optional<TradeChain> tradeChainOptional = tradeChainRepository.findById(tradeChainId);

        if (tradeChainOptional.isPresent()) {
            TradeChain tradeChain = tradeChainOptional.get();
            Session session = tradeChain.getSession();
            if (session != null && session.getStatus() == 2) {
                User sender = session.getSender();
                User receiver = session.getReceiver();
                List<Item> tradedItems = tradeChain.getItems();

                List<Item> senderItems = new ArrayList<>();
                List<Item> receiverItems = new ArrayList<>();
                for (Item item : tradedItems) {
                    if (sender.getInventory().getItems().contains(item)) {
                        senderItems.add(item);
                    } else if (receiver.getInventory().getItems().contains(item)) {
                        receiverItems.add(item);
                    }
                }

                sender.getInventory().getItems().removeAll(senderItems);
                receiver.getInventory().getItems().removeAll(receiverItems);

                List<Item> senderItemsInfo = new ArrayList<>();
                for (Item item : receiverItems) {
                    Item itemInfo = new Item();
                    itemInfo.setName(item.getName());
                    itemInfo.setPrice(item.getPrice());
                    itemInfo.setImageName(item.getImageName());
                    senderItemsInfo.add(itemInfo);
                }

                List<Item> receiverItemsInfo = new ArrayList<>();
                for (Item item : senderItems) {
                    Item itemInfo = new Item();
                    itemInfo.setName(item.getName());
                    itemInfo.setPrice(item.getPrice());
                    itemInfo.setImageName(item.getImageName());
                    receiverItemsInfo.add(itemInfo);
                }

                List<Item> newItemsSender = itemRepository.saveAll(senderItemsInfo);
                sender.getInventory().getItems().addAll(newItemsSender);

                List<Item> newItemsReceiver = itemRepository.saveAll(receiverItemsInfo);
                receiver.getInventory().getItems().addAll(newItemsReceiver);
                tradeChainRepository.delete(tradeChain);
                sessionRepository.delete(session);

                userRepository.save(sender);
                userRepository.save(receiver);
            }
        }

        return "redirect:/received_trades";
    }


    @GetMapping("/received_trades")
    public String showReceivedTrades(Model model) {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            User receiver = currentUser.getUser();
            List<Session> receivedTrades = sessionRepository.findByReceiver(receiver);
            model.addAttribute("receivedTrades", receivedTrades);
        }
        return "received_trades";
    }

    @PostMapping("/cancel_received_trade")
    public String cancelReceivedTrade(@RequestParam Long tradeId) {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            User receiver = currentUser.getUser();
            Session session = sessionRepository.findById(tradeId).orElseThrow();

            TradeChain tradeChain = tradeChainRepository.findBySessionId(tradeId);
            if (tradeChain != null) {
                tradeChainRepository.delete(tradeChain);
            }

            sessionRepository.deleteById(tradeId);

            return "redirect:/received_trades";
        }
        return "redirect:/received_trades";
    }

    @GetMapping("/received_trade")
    public String viewReceivedTrade(@RequestParam("tradeChainId") Long tradeChainId, Model model) {
        Optional<TradeChain> tradeChainOptional = tradeChainRepository.findById(tradeChainId);
        if (tradeChainOptional.isPresent()) {
            TradeChain tradeChain = tradeChainOptional.get();
            model.addAttribute("tradeChain", tradeChain);

            Session session = tradeChain.getSession();
            if (session != null) {
                User sender = session.getSender();
                User receiver = session.getReceiver();

                List<Item> senderItems = new ArrayList<>();
                List<Item> receiverItems = new ArrayList<>();

                for (Item item : tradeChain.getItems()) {
                    if (sender != null && sender.getInventory() != null && sender.getInventory().getItems().contains(item)) {
                        senderItems.add(item);
                    }
                    if (receiver != null && receiver.getInventory() != null && receiver.getInventory().getItems().contains(item)) {
                        receiverItems.add(item);
                    }
                }

                model.addAttribute("senderItems", senderItems);
                model.addAttribute("receiverItems", receiverItems);
            }
        }
        return "received_trade";
    }



    @GetMapping("/trade_creation_process")
    public String showTradeCreationProcess(@RequestParam Long receiverId, Model model) {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            User sender = currentUser.getUser();

            // Reload sender and receiver from the database to get the latest state
            User freshSender = userRepository.findById(sender.getId()).orElseThrow();
            User receiver = userRepository.findById(receiverId).orElseThrow();

            Inventory senderInventory = freshSender.getInventory();
            Inventory receiverInventory = receiver.getInventory();
            String receiverUsername = receiver.getUsername();

            String receiverItemsTitle = receiverUsername + "'s items";

            model.addAttribute("receiverId", receiverId);
            model.addAttribute("senderInventory", senderInventory);
            model.addAttribute("receiverInventory", receiverInventory);
            model.addAttribute("receiverUsername", receiverItemsTitle);
        }

        return "trade_creation_process";
    }



    @PostMapping("/create_trade_final")
    public String createTradeFinal(@RequestParam Long receiverId,
                                   @RequestParam(name = "senderTradeItems") String senderTradeItems,
                                   @RequestParam(name = "receiverTradeItems") String receiverTradeItems,
                                   RedirectAttributes redirectAttributes) {
        Authentication authentication = authenticationFacade.getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails currentUser = (CustomUserDetails) authentication.getPrincipal();
            User sender = currentUser.getUser();

            if (sender.getId().equals(receiverId)) {
                return "redirect:/create_trade?error=self_trade";
            }

            List<Long> senderItems = Arrays.stream(senderTradeItems.split(","))
                    .filter(item -> !item.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            List<Long> receiverItems = Arrays.stream(receiverTradeItems.split(","))
                    .filter(item -> !item.isEmpty())
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            if (senderItems.isEmpty() && receiverItems.isEmpty()) {
                return "redirect:/create_trade_failure";
            }

            User receiver = userRepository.findById(receiverId).orElseThrow();

            Session tradeSession = new Session();
            tradeSession.setSender(sender);
            tradeSession.setReceiver(receiver);
            tradeSession.setStatus(2);
            sessionRepository.save(tradeSession);

            TradeChain tradeChain = new TradeChain();
            tradeChain.setSession(tradeSession);
            tradeChain.setUser(sender);

            List<Item> senderItemsList = itemRepository.findAllById(senderItems);
            List<Item> receiverItemsList = itemRepository.findAllById(receiverItems);

            List<Item> allItems = new ArrayList<>();
            allItems.addAll(senderItemsList);
            allItems.addAll(receiverItemsList);
            tradeChain.setItems(allItems);
            tradeChainRepository.save(tradeChain);
        }
        return "redirect:/create_trade";
    }

    @GetMapping("/create_trade_failure")
    public String showCreateTradeFailurePage(Model model) {
        model.addAttribute("errorMessage", "Something went wrong!");
        return "create_trade_failure";
    }

}
