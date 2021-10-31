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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public List<AdminModule> getModules() {
        return modules;
    }

    public void setModules(List<AdminModule> modules) {
        this.modules = modules;
    }

    /**
     * Mock user information
     *
     * @return mocked object
     */
    public static UserInfo empty() {
        var adminModule = new AdminModule();
        adminModule.setClassName("UserPanel");
        var userInfo = new UserInfo();
        userInfo.setName("Someone");
        userInfo.setPicture("images/faces/user-tie-solid.svg");
        userInfo.getModules().add(adminModule);
        return userInfo;
    }
}
