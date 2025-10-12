
package com.raven.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Customer nested object for Order
 */
public class Customer {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("email")
    private String email;
    
    // Constructors
    public Customer() {
    }
    
    public Customer(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    @Override
    public String toString() {
        return "Customer{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
