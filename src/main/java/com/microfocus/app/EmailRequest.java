package com.microfocus.app;

public class EmailRequest {

    private String server;
    private Integer port;
    private String username;
    private String password;
    private String to;
    private String from;
    private String bounce;
    private String subject;
    private String body;
    private boolean debug;

    EmailRequest(String server, Integer port, String to, String from, String subject, String body) {
        this.server = server;
        this.port = port;
        this.to = to;
        this.from = from;
        this.subject = subject;
        this.body = body;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
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

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getBounce() {
        return bounce;
    }

    public void setBounce(String bounce) {
        this.bounce = bounce;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean getDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public String toString() {
        return "EmailRequest{" +
                "server='" + server + '\'' +
                ", port='" + port + '\'' +
                ", to='" + to + '\'' +
                ", from='" + from + '\'' +
                '}';
    }

}
