package com.smart.controller;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.razorpay.*;
import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.persistence.criteria.Order;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private MyOrderRepository myOrderRepository;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private UserRepository userRepository;

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		if (principal != null) {
			String userName = principal.getName();
			User user = userRepository.getUserByUserName(userName);
			model.addAttribute("user", user);
		}
	}

	@GetMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("title", "Dashboard");
		return "normal/user_dashboard";
	}

	@GetMapping("/add-contact")
	public String openAddContactForm(Model model, HttpSession session) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		// 🔥 REMOVE FLASH MESSAGE AFTER DISPLAY
		if (session.getAttribute("message") != null) {
			session.removeAttribute("message");
		}

		return "normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {
			User user = userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);

			// ---------- FILE UPLOAD ----------
			if (!file.isEmpty()) {
				File saveDir = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveDir.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			} else {
				contact.setImage("contact.png");
			}

			user.getContacts().add(contact);
			userRepository.save(user);

			session.setAttribute("message", new Message("Contact added successfully !!", "success"));

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong !!", "danger"));
		}

		// 🔁 POST → REDIRECT → GET (PREVENT DUPLICATE SUBMIT)
		return "redirect:/user/add-contact";
	}

	// show contact handler
	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page") Integer page, Model m, Principal principal) {
		m.addAttribute("title", "show new user contacts");
		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);
		PageRequest pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactRepository.findByUserId(user.getId(), pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPage", contacts.getTotalPages());

		return "normal/show_contacts";
	}

	@GetMapping("/delete-contact/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cid, Principal principal, HttpSession session) {

		Contact contact = this.contactRepository.findById(cid).get();

		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);

		if (user.getId() == (contact.getUser().getId())) {

			// 🔥 Remove from user → orphanRemoval auto delete from DB
			user.getContacts().remove(contact);
			this.userRepository.save(user);

			session.setAttribute("message", new Message("Contact deleted successfully!", "success"));
		}

		return "redirect:/user/show-contacts/0";
	}

	// showing perticular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);

		System.out.println("CID:" + cId);
		Optional<Contact> contactOpt = this.contactRepository.findById(cId);
		Contact contact = contactOpt.get();

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);

		}

		return "normal/contact_detais";

	}

	// update form open
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId, Model model) {

		model.addAttribute("title", "update contact");
		Contact contact = this.contactRepository.findById(cId).get();

		model.addAttribute("contact", contact);
		return "normal/update_form";
	}

	// update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {

			Contact oldContact = contactRepository.findById(contact.getcId()).get();

			// ---------- IMAGE UPDATE ----------
			if (!file.isEmpty()) {

				// delete old image (except default)
				File deleteDir = new ClassPathResource("static/image").getFile();
				File oldFile = new File(deleteDir, oldContact.getImage());

				if (oldFile.exists() && !oldContact.getImage().equals("contact.png")) {
					oldFile.delete();
				}

				// save new image
				File saveDir = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveDir.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename());

			} else {
				// no new file → keep old image
				contact.setImage(oldContact.getImage());
			}

			// ---------- USER SET ----------
			User user = userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);

			contactRepository.save(contact);

			session.setAttribute("message", new Message("Contact updated successfully!", "success"));

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Update failed! Try again.", "danger"));
		}

		return "redirect:/user/" + contact.getcId() + "/contact";
	}

	// profile handller
	@GetMapping("/profile")
	public String profileHandller(Model model) {
		model.addAttribute("title", "profile page");

		return "/normal/profile";
	}

	// open setting handler

	@GetMapping("/settings")
	public String openSetting() {

		return "normal/setting";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {

		System.out.println("old pass :" + oldPassword);
		System.out.println("new pass :" + newPassword);

		String username = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(username);

		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			// change password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);

			session.setAttribute("message", new Message("yout password is updated....", "alert-success"));

		} else {
			// error
			session.setAttribute("message", new Message("please enter correct old password....", "alert-danger"));
			return "redirect:/user/settings";

		}

		return "redirect:/user/index";
	}

	@PostMapping("/create-order")
	@ResponseBody
	public Map<String,Object> createOrder(@RequestBody Map<String,Object> data, Principal principal) throws Throwable {
	    String username = principal.getName();
	    User user = this.userRepository.getUserByUserName(username);

	    System.out.println("order created ..............."+data);

	    int amt = Integer.parseInt(data.get("amount").toString()); // frontend input in rupees

	    var client = new RazorpayClient("rzp_test_S0xDK5WMV5khED",
	                                    "Uy5iWzmBjj3Zktzl3bsVWEL3");

	    JSONObject ob = new JSONObject();
	    ob.put("amount", amt*100); // convert to paise
	    ob.put("currency", "INR");
	    ob.put("receipt", "txn_" + System.currentTimeMillis()); // unique receipt

	    com.razorpay.Order orders = client.orders.create(ob);

	    System.out.println(orders);

	    // save order in DB
	    MyOrder myOrder = new MyOrder();
	    myOrder.setAmount(orders.get("amount")+"");
	    myOrder.setOrderId(orders.get("id"));
	    myOrder.setPaymentId(null);
	    myOrder.setStatus("created");
	    myOrder.setUser(user);
	    myOrder.setReceipt(orders.get("receipt"));
	    this.myOrderRepository.save(myOrder);

	    // return to frontend
	    Map<String, Object> map = new HashMap<>();
	    map.put("id", orders.get("id"));
	    map.put("amount", orders.get("amount"));
	    map.put("currency", orders.get("currency"));
	    return map;
	}

	 

	/*
	 * @PostMapping("/create-order")
	 * 
	 * @ResponseBody public ResponseEntity<?> createOrder(@RequestBody Map<String,
	 * Object> data) {
	 * 
	 * try { int amount = Integer.parseInt(data.get("amount").toString());
	 * 
	 * RazorpayClient client = new RazorpayClient( "rzp_test_RzNrEWq2uYedQa",
	 * "zQXPSpYcAq208xiGuUH7w0am" );
	 * 
	 * JSONObject options = new JSONObject(); options.put("amount", amount * 100);
	 * options.put("currency", "INR"); options.put("receipt", "txn_" +
	 * System.currentTimeMillis());
	 * 
	 * com.razorpay.Order order = client.orders.create(options);
	 * 
	 * Map<String, Object> response = new HashMap<>(); response.put("id",
	 * order.get("id")); response.put("amount", order.get("amount"));
	 * response.put("currency", order.get("currency")); response.put("status",
	 * "created");
	 * 
	 * return ResponseEntity.ok(response);
	 * 
	 * } catch (Exception e) { e.printStackTrace(); return
	 * ResponseEntity.status(500).body("ERROR : " + e.getMessage()); } }
	 */
	
	
	
	
	
	@PostMapping("/update-order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data) {
	    
	    // Fetch order by ID
	    MyOrder myOrder = this.myOrderRepository.findByorderId((String) data.get("order_id"));
	    
	    if (myOrder == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order not found");
	    }

	    // Update fields
	    myOrder.setPaymentId((String) data.get("payment_id"));
	    myOrder.setStatus((String) data.get("status"));
	    
	    // Save updated order
	    this.myOrderRepository.save(myOrder);
	    
	    return ResponseEntity.ok("Order updated successfully");
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
