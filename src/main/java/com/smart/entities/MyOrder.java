package com.smart.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="orders")
public class MyOrder {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long myOrderid;
	
	private String orderId;
	private String receipt;
	private String amount;
	private String status;
	
	@ManyToOne
	private User user;
	public MyOrder() {
		super();
		// TODO Auto-generated constructor stub
	}
	public MyOrder(Long myOrderid, String orderId, String receipt, String amount, String status, User user,
			String paymentId) {
		super();
		this.myOrderid = myOrderid;
		this.orderId = orderId;
		this.receipt = receipt;
		this.amount = amount;
		this.status = status;
		this.user = user;
		this.paymentId = paymentId;
	}
	@Override
	public String toString() {
		return "MyOrder [myOrderid=" + myOrderid + ", orderId=" + orderId + ", receipt=" + receipt + ", amount="
				+ amount + ", status=" + status + ", user=" + user + ", paymentId=" + paymentId + "]";
	}
	public Long getMyOrderid() {
		return myOrderid;
	}
	public void setMyOrderid(Long myOrderid) {
		this.myOrderid = myOrderid;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getReceipt() {
		return receipt;
	}
	public void setReceipt(String receipt) {
		this.receipt = receipt;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public String getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(String paymentId) {
		this.paymentId = paymentId;
	}
	private String paymentId;
	
	
	

}
