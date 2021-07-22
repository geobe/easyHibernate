package de.geobe.architecture.test;

import javax.persistence.Entity;

@Entity(name = "OrgAdd")
public class OrganisationalAddress extends AddressBase {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OrganisationalAddress(String nickname, String name) {
        super(nickname);
        this.name = name;
    }

    public OrganisationalAddress() {
    }
}
