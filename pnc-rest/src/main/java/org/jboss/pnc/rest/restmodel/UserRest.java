package org.jboss.pnc.rest.restmodel;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.jboss.pnc.model.User;
import org.jboss.pnc.model.builder.UserBuilder;

@XmlRootElement(name = "User")
public class UserRest {

    private Integer id;

    private String email;

    private String firstName;

    private String lastName;

    private String username;

    public UserRest() {
    }

    public UserRest(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.username = user.getUsername();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @XmlTransient
    public User toUser() {
        UserBuilder builder = UserBuilder.newBuilder();
        builder.username(username);
        builder.email(email);
        builder.firstName(firstName);
        builder.lastName(lastName);
        return builder.build();
    }

}
