package com.iapp.ageofchess.multiplayer.webutil;

public class SocketRequest {

    private final String request;
    private final String[] parameters;
    private long id = -1;

    public SocketRequest(String request, String... parameters) {
        this.request = request;
        this.parameters = parameters;
    }

    public String getRequest() {
        return request;
    }

    public String[] getParameters() {
        return parameters;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
}
