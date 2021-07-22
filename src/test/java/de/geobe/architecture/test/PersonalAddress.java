package de.geobe.architecture.test;

import javax.persistence.Entity;
import java.time.Instant;

@Entity(name = "PersAdd")
public class PersonalAddress extends AddressBase {
    private String firstName;
    private String lastName;
    private Instant bornAt;

    public PersonalAddress(String nickname, String firstName, String lastName, Instant bornAt) {
        super(nickname);
        this.firstName = firstName;
        this.lastName = lastName;
        this.bornAt = bornAt;
    }

    public PersonalAddress() {
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

    public Instant getBornAt() {
        return bornAt;
    }

    public void setBornAt(Instant bornAt) {
        this.bornAt = bornAt;
    }
}
