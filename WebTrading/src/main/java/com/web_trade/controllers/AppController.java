package com.web_trade.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//import com.web_trade.entity.Product;
//import com.web_trade.entity.TradeProduct;
import com.web_trade.entity.Inventory;
import com.web_trade.entity.Item;
import com.web_trade.repository.InventoryRepository;
import com.web_trade.repository.UserRepository;
import com.web_trade.repository.ItemRepository;
import com.web_trade.entity.User;
import com.web_trade.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AppController {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private InventoryRepository inventoryRepo;

	@Autowired
	private ItemRepository itemRepo;


	@GetMapping("")
	public String viewHomePage() {
		return "index";
	}

	@GetMapping("/login2")
	public String viewAfterLogin() {
		return "login";
	}

	@GetMapping("/mainpage")
	public String mainPage() {
		return "mainpage";
	}
	
	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user", new User());
		return "signup_form";
	}

	@PostMapping("/process_register")
	public String processRegister(User user) {
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);

		Inventory inv = new Inventory(null, new ArrayList<>());
		inventoryRepo.save(inv);
		user.setInventory(inv);
		userRepo.save(user);

		return "register_success";
	}





}
