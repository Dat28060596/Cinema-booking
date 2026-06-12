package com.cinema.domain.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(length = 20)
    private String salutation;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private boolean needInvoice;

    @Column(length = 50)
    private String accountType;

    private boolean newsletter;

    public Customer() {}

    public Customer(String email, String salutation, String firstName, String lastName, boolean needInvoice, String accountType, boolean newsletter) {
        this.email = email;
        this.salutation = salutation;
        this.firstName = firstName;
        this.lastName = lastName;
        this.needInvoice = needInvoice;
        this.accountType = accountType;
        this.newsletter = newsletter;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSalutation() { return salutation; }
    public void setSalutation(String salutation) { this.salutation = salutation; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public boolean isNeedInvoice() { return needInvoice; }
    public void setNeedInvoice(boolean needInvoice) { this.needInvoice = needInvoice; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public boolean isNewsletter() { return newsletter; }
    public void setNewsletter(boolean newsletter) { this.newsletter = newsletter; }
}
