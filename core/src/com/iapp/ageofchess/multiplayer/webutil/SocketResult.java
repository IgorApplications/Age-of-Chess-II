package com.iapp.ageofchess.multiplayer.webutil;

public class SocketResult {

    private final RequestStatus status;
    private final String result;
    private final String request;

    public SocketResult(RequestStatus status, String result, String request) {
        this.status = status;
        this.result = result;
        this.request = request;
    }

    public SocketResult(RequestStatus status, String request) {
        this.status = status;
        this.result = null;
        this.request = request;
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
}
