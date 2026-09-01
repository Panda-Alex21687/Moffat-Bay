package com.moffatbaymarina.model;

/**
 * Represents a registered Moffat Bay Marina customer.
 *
 * This class maps to the customers table in the database.
 * Passwords are never stored as plain text. The passwordHash field
 * contains only the BCrypt hash saved by the registration process.
 */
public class Customer {

    private long customerId;
    private String firstName;
    private String lastName;
    private String phone;
    private String street;
    private String city;
    private String state;
    private String zip;
    private String email;
    private String passwordHash;

    /**
     * Required by JavaBean/Jackson conventions.
     */
    public Customer() {
    }

    /**
     * Constructor used before a customer has been inserted into the database.
     */
    public Customer(String firstName,
            String lastName,
            String phone,
            String street,
            String city,
            String state,
            String zip,
            String email,
            String passwordHash) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    /**
     * Constructor used when loading a complete customer from the database.
     */
    public Customer(long customerId,
            String firstName,
            String lastName,
            String phone,
            String street,
            String city,
            String state,
            String zip,
            String email,
            String passwordHash) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId=" + customerId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phone='" + phone + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zip='" + zip + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
