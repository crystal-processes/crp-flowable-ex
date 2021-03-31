package org.crp.flowable.shell.utils;

public class Configuration {
    private String login;
    private String password;
    private String restURL;
    private String idmURL;

    public String getRestURL() {
        return restURL;
    }

    public void setRestURL(String restURL) {
        this.restURL = restURL;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getIdmURL() {
        return idmURL;
    }

    public void setIdmURL(String idmURL) {
        this.idmURL = idmURL;
    }
}
