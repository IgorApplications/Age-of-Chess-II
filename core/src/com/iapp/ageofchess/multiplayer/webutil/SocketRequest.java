package com.iapp.ageofchess.multiplayer.webutil;

public class SocketRequest {

    private final String request;
    private final String[] parameters;

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
}
