package model;

import io.ebean.annotation.DbDefault;
import io.ebean.annotation.NotNull;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="users")
public class User {
    @Id
    private long userId;
    @NotNull
    private String username;
    @NotNull @DbDefault("2")
    private int role = 2;
    @ManyToMany
    @JoinTable(
            name = "user_museum",
            joinColumns = @JoinColumn(name = "fk_user_id", referencedColumnName = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "fk_museum_id", referencedColumnName = "museum_id"))
    private Set<Museum> ownedMuseums;

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public int getRole() {
        return role;
    }

    public Set<Museum> getOwnedMuseums() {
        return ownedMuseums;
    }

    public void updateWith(User newUser) {
        this.username = newUser.getUsername();
    }

}
