package com.vityarthi.banking.model;

import java.io.Serializable;

/**
 * Entity representing a bank customer / user.
 * Demonstrates Unit 2: Encapsulation, Constructors, Access Modifiers.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String address;

    public User(String userId, String fullName, String email, String phoneNumber, String address) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return String.format("User[ID=%s, Name='%s', Email='%s', Phone='%s']",
                userId, fullName, email, phoneNumber);
    }
}
