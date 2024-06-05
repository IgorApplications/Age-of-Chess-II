package com.iapp.lib.web;

public class SocketResult {

    private final RequestStatus status;
    private final String result;
    private final String request;
    private final long id;

    public SocketResult(RequestStatus status, String result, SocketRequest req) {
        this.status = status;
        this.result = result;
        request = req.getRequest();
        id = req.getSenderId();
    }

    public SocketResult(RequestStatus status, SocketRequest req) {
        this.status = status;
        this.result = null;
        request = req.getRequest();
        id = req.getSenderId();
    }

    public RequestStatus getStatus() {
        return status;
    }

    public String getResult() {
        return result;
    }

    public String getRequest() {
        return request;
    }

    public long getId() {
        return id;
    }
}
