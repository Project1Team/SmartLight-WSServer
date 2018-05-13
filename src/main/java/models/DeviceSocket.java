package models;

import org.java_websocket.WebSocket;

public class DeviceSocket {
    WebSocket conn;
    String macAdrr;

    public DeviceSocket(WebSocket conn, String macAdrr) {
        this.conn = conn;
        this.macAdrr = macAdrr;
    }

    public WebSocket getConn() {
        return conn;
    }

    public void setConn(WebSocket conn) {
        this.conn = conn;
    }

    public String getMacAdrr() {
        return macAdrr;
    }

    public void setMacAdrr(String macAdrr) {
        this.macAdrr = macAdrr;
    }
}

