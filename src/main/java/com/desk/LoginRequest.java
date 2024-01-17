package com.desk;


public class LoginRequest {
    private String login;
    private String password;

    // Constructeur par défaut (peut être nécessaire pour la désérialisation)
    public LoginRequest() {
    }

    // Constructeur avec tous les champs
    public LoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    // Getters et Setters
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
