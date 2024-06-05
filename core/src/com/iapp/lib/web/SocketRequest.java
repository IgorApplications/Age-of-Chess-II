package com.iapp.lib.web;

public class SocketRequest {

    private final String request;
    private final String[] parameters;
    private long senderId = -1;

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

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public long getSenderId() {
        return senderId;
    }
}
