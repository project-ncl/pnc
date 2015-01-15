/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.pnc.model.builder;

import java.util.ArrayList;
import java.util.List;

import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.User;

/**
 * @author avibelli
 *
 */
public class UserBuilder {

    private Integer id;

    private String email;

    private String firstName;

    private String lastName;

    private String username;

    private List<BuildRecord> buildRecords;

    private UserBuilder() {
        buildRecords = new ArrayList<>();
    }

    public static UserBuilder newBuilder() {
        return new UserBuilder();
    }

    public User build() {

        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);

        // Set the bi-directional mapping
        for (BuildRecord buildRecord : buildRecords) {
            buildRecord.setUser(user);
        }
        user.setBuildRecords(buildRecords);

        return user;
    }

    public UserBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public UserBuilder email(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public UserBuilder lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public UserBuilder username(String username) {
        this.username = username;
        return this;
    }

    public UserBuilder buildRecord(BuildRecord buildRecord) {
        this.buildRecords.add(buildRecord);
        return this;
    }

    public UserBuilder buildRecords(List<BuildRecord> buildRecords) {
        this.buildRecords = buildRecords;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public List<BuildRecord> getBuildRecords() {
        return buildRecords;
    }

}
