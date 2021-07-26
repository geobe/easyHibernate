package de.geobe.architecture.test;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "AddBase")
@Inheritance(strategy = InheritanceType.JOINED)
public class AddressBase {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    private String nickname;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private Set<Communication> comms = new HashSet<>();

    public Set<Communication> getComms() {
        return Collections.unmodifiableSet(comms);
    }

    public boolean addComm(Communication comm) {
        return this.comms.add(comm);
    }

    public boolean deleteComm(Communication comm) {
        return this.comms.remove(comm);
    }

    public AddressBase(String nickname) {
        this.nickname = nickname;
    }

    public AddressBase() { }

    public long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
