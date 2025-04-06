package edu.northeastern.mindyourmoneyapp;

public class UserClass {

    String name, email, username, password;
    int setBudget;
    int rewards;
    public UserClass(String name, String email, String username, String password,int setBudget,int rewards) {
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.setBudget = setBudget;
        this.rewards = rewards;
    }
    public UserClass() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public int getBudget() {
        return setBudget;
    }
    public void setBudget(int budget) {
        this.setBudget = budget;
    }
    public void setRewards(int rewards){ this.rewards = rewards;}

    public int getRewards(){ return rewards;}
}