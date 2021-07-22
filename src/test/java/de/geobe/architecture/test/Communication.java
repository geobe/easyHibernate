package de.geobe.architecture.test;

import javax.persistence.*;

@Entity(name = "Comm")
public class Communication {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private CommType commType;
    private String locator;
    private String note;
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private AddressBase owner;

    public Communication(CommType commType, String locator, String note) {
        this.commType = commType;
        this.locator = locator;
        this.note = note;
    }

    public Communication() {
    }

    public long getId() {
        return id;
    }

    public CommType getCommType() {
        return commType;
    }

    public void setCommType(CommType commType) {
        this.commType = commType;
    }

    public String getLocator() {
        return locator;
    }

    public void setLocator(String locator) {
        this.locator = locator;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public AddressBase getOwner() {
        return owner;
    }

    public void setOwner(AddressBase owner) {
        this.owner = owner;
    }
}
