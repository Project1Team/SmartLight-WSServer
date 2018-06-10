import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.*;
import org.bson.types.ObjectId;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import models.DeviceSocket;
import models.UserSocket;


public class Server extends WebSocketServer {

    private static MongoDBUser mongoDBUser;

    private String[] requests;

    // host and port of mongoDB
    private static final String HOST = "localhost";
    private static final int PORT_DB = 27017;

    // Username and password connect to MongoDB
    private static final String USERNAME = "thongle";
    private static final String PASSWORD = "123321";

    // name database and collection name of database
    private static final String DB_NAME = "smartLight";
    private static final String COLLECTION_NAME = "user";

    //list users & devices Socket connect with server
    public static List<UserSocket> userSocketList = new ArrayList<UserSocket>();
    public static List<DeviceSocket> deviceSocketList = new ArrayList<DeviceSocket>();

    // port of server
    private static final int PORT_SERVER = 8888;

    public Server(int port ) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
    }

    public Server(InetSocketAddress address ) {
        super( address );
    }

    public static void main( String[] args ) throws InterruptedException , IOException {

        WebSocketImpl.DEBUG = true;
        int port = PORT_SERVER;
        try {
            port = Integer.parseInt( args[ 0 ] );
        } catch ( Exception ex ) {
        }
        Server s = new Server( port );
        s.start();
        System.out.println( "Server started on port: " + s.getPort() );

        MongoClientURI uri = new MongoClientURI("mongodb://" + USERNAME +":" + PASSWORD + "@" + HOST +":" + PORT_DB + "/?authSource=admin");
        MongoClient mongoClient = new MongoClient(uri);

        DB database = mongoClient.getDB(DB_NAME);
        DBCollection collection = database.getCollection(COLLECTION_NAME);

        mongoDBUser = new MongoDBUser(collection);
//
//        DBObject dbUser = mongoDBUser.getUserDBObject("thao", "111111");
//        System.out.println(dbUser.toString());
    }

    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        broadcast( "new connection: " + handshake.getResourceDescriptor() );
        System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!" );
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {

        // remove user socket in user socket list if it is user socket
        for(UserSocket userSocket: userSocketList){
            if(userSocket.getConn().equals(conn)){
                userSocketList.remove(userSocket);
                return;
            }
        }
        // remove device socket in device socket list if it is device socket
        for (DeviceSocket deviceSocket: deviceSocketList){
            if(deviceSocket.getConn().equals(conn)){
                deviceSocketList.remove(deviceSocket);
                return;
            }
        }

        System.out.println( conn + " has close!" );
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
        System.out.println( conn + ": " + message );
        requests = message.split("/");
        switch (requests[0]){

            case "login":
                String[] account = requests[1].split("@");
                int checkAccount =  mongoDBUser.checkLoginUser(account[0], account[1], userSocketList);

                conn.send("login/" + checkAccount);

                if(checkAccount == 1) {
                    DBObject dbUser = mongoDBUser.getUserDBObject(account[0],account[1]);

                    // add user socket to list user socket
                    UserSocket userSocket = new UserSocket(conn, (ObjectId) dbUser.get("_id"));
                    userSocketList.add(userSocket);

                    System.out.println(dbUser.toString());
                    conn.send("getUser/" + dbUser.toString());
                }
                else
                    System.out.print("Fail !");
                break;

            case "logout":
                for(UserSocket userSocket: userSocketList){
                    if(userSocket.getConn().equals(conn)){
                        userSocketList.remove(userSocket);
                        return;
                    }
                }
                break;

            case "deviceConnect":
                DeviceSocket deviceSocket_conn = new DeviceSocket(conn, requests[1]);
                deviceSocketList.add(deviceSocket_conn);
                break;

            case "changeColor":
                DeviceSocket deviceSocket_changeCl = Utils.getSocketDeviceByMacAddr(requests[1]);
                if(deviceSocket_changeCl != null){
                    deviceSocket_changeCl.getConn().send("changeColor/"+requests[2]);
                    conn.send("messageRes/Sent Color");
                    //update color to DB
                    mongoDBUser.updateColorDb(Utils.getObjectIdBySocket(conn), requests[1], requests[2]);
                }
                else
                    conn.send("messageRes/Not found Device");
                break;

            case "reset":
                DeviceSocket deviceSocket_reset = Utils.getSocketDeviceByMacAddr(requests[1]);
                if(deviceSocket_reset != null){
                    deviceSocket_reset.getConn().send("changeColor/000255255255255");
                    conn.send("messageRes/Reseted light");
                    //update color to DB
                    mongoDBUser.updateColorDb(Utils.getObjectIdBySocket(conn), requests[1], "000255255255255");
                }
                else
                    conn.send("messageRes/Not found Device");
                break;

            case "turnOff":
                DeviceSocket deviceSocket_off = Utils.getSocketDeviceByMacAddr(requests[1]);
                if(deviceSocket_off != null){
                    deviceSocket_off.getConn().send("changeColor/000000000000000");
                    conn.send("messageRes/Turned off light");
                }
                else
                    conn.send("messageRes/Not found Device");
                break;

            case "turnOn":
                DeviceSocket deviceSocket_on = Utils.getSocketDeviceByMacAddr(requests[1]);
                if(deviceSocket_on != null){
                    String colorCurrent = mongoDBUser.getColorByMacAddress(Utils.getObjectIdBySocket(conn), requests[1]);
                    deviceSocket_on.getConn().send("turnOn/" + colorCurrent);
                    conn.send("messageRes/Turned on light");
                }
                else
                    conn.send("messageRes/Not found Device");
                break;

            case "changePassword":
                if (mongoDBUser.changePassword(Utils.getObjectIdBySocket(conn), requests[1], requests[2]))
                    conn.send("messageRes/changed password successful");
                else
                    conn.send("messageRes/changed password failed");
                break;

            case "renameRoom":
                if (mongoDBUser.renameRoom(Utils.getObjectIdBySocket(conn), requests[1], requests[2]))
                    conn.send("messageRes/rename room successful");
                else
                    conn.send("messageRes/rename room failed");
                break;

            case "renameDevice":
                if (mongoDBUser.renameDevice(Utils.getObjectIdBySocket(conn), requests[1], requests[2]))
                    conn.send("messageRes/rename device successful");
                else
                    conn.send("messageRes/rename device failed");
                break;

            case "updateProfile":
                break;

            case "getColor":
        }
    }
    @Override
    public void onMessage( WebSocket conn, ByteBuffer message ) {
        broadcast( message.array() );
//        System.out.println( conn + ": " + message );
    }


    @Override
    public void onFragment( WebSocket conn, Framedata fragment ) {
        System.out.println( "received fragment: " + fragment );
    }

    @Override
    public void onError( WebSocket conn, Exception ex ) {
        ex.printStackTrace();
        System.out.println("Server error!");
        if( conn != null ) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart() {
        System.out.println("Server started!");
    }

}