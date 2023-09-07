package io.keepup.plugins.adminui.rest.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Information about user
 */
public class UserInfo {
    private String name;
    private String picture;
    private List<AdminModule> modules;

    /**
     * Default constructor, initializes user's modules as an empty list
     */
    public UserInfo() {
        modules = new ArrayList<>();
    }

    /**
     * @return user's name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name user's nickname
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return user's avatar
     */
    public String getPicture() {
        return picture;
    }

    /**
     * @param picture user's avatar
     */
    public void setPicture(String picture) {
        this.picture = picture;
    }

    /**
     * @return list of UI admin modules available for user
     */
    public List<AdminModule> getModules() {
        return modules;
    }

    /**
     * @param modules list of UI modules available for user
     */
    public void setModules(List<AdminModule> modules) {
        this.modules = modules;
    }

    @Override
    public String toString() {
        final var moduleStringBuilder = new StringBuilder();
        modules.forEach(module -> moduleStringBuilder.append(module.toString()));
        return "{\"name\": \"%s\", \"picture\": \"%s\", \"modules\":\"%s\"}"
                .formatted(name, picture, moduleStringBuilder.toString());
    }

    /**
     * Mock user information
     *
     * @return mocked object
     */
    public static UserInfo empty() {
        var adminModule = new AdminModule("UserPanel");
        var userInfo = new UserInfo();
        userInfo.setName("Anonymous");
        userInfo.setPicture("images/faces/user-tie-solid.svg");
        userInfo.getModules().add(adminModule);
        return userInfo;
    }
}