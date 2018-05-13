package models;

import org.bson.types.ObjectId;
import org.java_websocket.WebSocket;

public class UserSocket {
    WebSocket conn;
    ObjectId id;

    public UserSocket(WebSocket conn, ObjectId id) {
        this.conn = conn;
        this.id = id;
    }

    public WebSocket getConn() {
        return conn;
    }

    public void setConn(WebSocket conn) {
        this.conn = conn;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
