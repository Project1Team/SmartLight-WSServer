import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.*;
import org.bson.types.ObjectId;
import org.java_websocket.WebSocket;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import models.DeviceSocket;
import models.UserSocket;


public class Server extends WebSocketServer {

    private static MongoDBUser mongoDBUser;

    private String[] requests;

    // host and port of mongoDB
    private static final String HOST = "ds149221.mlab.com";
    private static final int PORT_DB = 49221;

    // Username and password connect to MongoDB
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin123";

    // name database and collection name of database
    private static final String DB_NAME = "meshsmarthome";
    private static final String COLLECTION_NAME = "user";

    //list users & devices Socket connect with server
    public static List<UserSocket> userSocketList = new ArrayList<UserSocket>();
    public static List<DeviceSocket> deviceSocketList = new ArrayList<DeviceSocket>();

    // port of server
    private static final int PORT_SERVER = 8080;

    public Server(int port ) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
    }

    public Server(InetSocketAddress address ) {
        super( address );
    }

    public static void main( String[] args ) throws InterruptedException , IOException {

          //WebSocketImpl.DEBUG = true;
        int port = PORT_SERVER;
        try {
            port = Integer.parseInt( args[ 0 ] );
        } catch ( Exception ex ) {
        }
        Server s = new Server(  port );
        s.start();
        System.out.println( "Server started on port: " + s.getPort() );

        MongoClientURI uri = new MongoClientURI("mongodb://" + USERNAME +":" + PASSWORD + "@" + HOST +":" + PORT_DB + "/" + DB_NAME);
        MongoClient mongoClient = new MongoClient(uri);

        DB database = mongoClient.getDB(DB_NAME);
        DBCollection collection = database.getCollection(COLLECTION_NAME);

        mongoDBUser = new MongoDBUser(collection);
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
//                if (mongoDBUser.getCurrentStatusByMacAddr(requests[1]).equals("on"))
//                    conn.send("changeColor/" + mongoDBUser.getCurrentColorByMacAddress(requests[1]) + "255" + mongoDBUser.getCurrentBrightnessByMacAddr(requests[1]));
//                else
//                    conn.send("changeColor/000000000000000");
                break;

            case "switch":
                DeviceSocket deviceSocket_changeStatus = Utils.getSocketDeviceByMacAddr(requests[1]);
                if(deviceSocket_changeStatus != null){
                    if(mongoDBUser.updateStatusSwitch(Utils.getObjectIdBySocket(conn), requests[1], requests[2])){
                        deviceSocket_changeStatus.getConn().send("switch/"+requests[2]);
                    }
                    else{
                        conn.send("messageRes/Can't control switch");
                    }
                    //update color to DB
//                    if(mongoDBUser.updateColorDb(Utils.getObjectIdBySocket(conn), requests[1], requests[2]))
//                        conn.send("updateColor/" + requests[1] + "/" + requests[2]);
                }
                else
                    conn.send("messageRes/Device not found");
                break;

            case "temperature":
                for(UserSocket userSocket : userSocketList){
                    if(mongoDBUser.updateTemperate(userSocket.getId(), Utils.getMacAddrBySocket(conn), requests[1])){
                        DBObject dbUser = mongoDBUser.getUserDBObjectById(userSocket.getId());
                        userSocket.getConn().send("update/" + dbUser.toString());
                    }
                }
                break;

            case "fire":
                for(UserSocket userSocket : userSocketList){
                    if(mongoDBUser.updateFire(userSocket.getId(), Utils.getMacAddrBySocket(conn), requests[1])){
                        DBObject dbUser = mongoDBUser.getUserDBObjectById(userSocket.getId());
                        userSocket.getConn().send("update/" + dbUser.toString());
                        break;
                    }
                }
                break;

            case "gas":
                for(UserSocket userSocket : userSocketList){
                    if(mongoDBUser.updateGas(userSocket.getId(), Utils.getMacAddrBySocket(conn), requests[1])){
                        DBObject dbUser = mongoDBUser.getUserDBObjectById(userSocket.getId());
                        userSocket.getConn().send("update/" + dbUser.toString());
                        break;
                    }
                }
            case "resetFire":
                DeviceSocket deviceSocket_resetFire = Utils.getSocketDeviceByMacAddr(requests[1]);
                if(deviceSocket_resetFire != null){
                    if(mongoDBUser.resetFire(Utils.getObjectIdBySocket(conn), requests[1])){
                        deviceSocket_resetFire.getConn().send("resetFire");
                        for(UserSocket userSocket : userSocketList){
                            if(userSocket.getConn().equals(conn)){
                                DBObject dbUser = mongoDBUser.getUserDBObjectById(userSocket.getId());
                                conn.send("update/" + dbUser.toString());
                                break;
                            }
                        }
                    }
                    else{
                        conn.send("messageRes/Can't reset");
                    }
                }
                else
                    conn.send("messageRes/Device not found");
                break;

            case "resetGas":
                DeviceSocket deviceSocket_resetGas = Utils.getSocketDeviceByMacAddr(requests[1]);
                if(deviceSocket_resetGas != null){
                    if(mongoDBUser.resetGas(Utils.getObjectIdBySocket(conn), requests[1])){
                        deviceSocket_resetGas.getConn().send("resetGas");
                        for(UserSocket userSocket : userSocketList){
                            if(userSocket.getConn().equals(conn)){
                                DBObject dbUser = mongoDBUser.getUserDBObjectById(userSocket.getId());
                                conn.send("update/" + dbUser.toString());
                                break;
                            }
                        }
                    }
                    else{
                        conn.send("messageRes/Can't reset");
                    }
                }
                else
                    conn.send("messageRes/Device not found");
                break;

//            case "changeBrightness":
//                DeviceSocket deviceSocket_changeBr = Utils.getSocketDeviceByMacAddr(requests[1]);
//                if(deviceSocket_changeBr != null){
//                    String color = mongoDBUser.getColorByMacAddress(Utils.getObjectIdBySocket(conn), requests[1]);
//                    deviceSocket_changeBr.getConn().send("changeColor/" + color + "255" + requests[2]);
//                    //update color to DB
//                    if (mongoDBUser.updateBrightnessDb(Utils.getObjectIdBySocket(conn), requests[1], requests[2]))
//                        conn.send("updateBrightness/" + requests[1] + "/" + requests[2]);
//                }
//                else
//                    conn.send("messageRes/Device not found");
//                break;

//            case "reset":
//                DeviceSocket deviceSocket_reset = Utils.getSocketDeviceByMacAddr(requests[1]);
//                if(deviceSocket_reset != null){
//                    deviceSocket_reset.getConn().send("changeColor/255255255255255");
//                    conn.send("messageRes/Light has been reset");
//                    //update color to DB
//                    mongoDBUser.updateColorDb(Utils.getObjectIdBySocket(conn), requests[1], "255255255255255");
//                }
//                else
//                    conn.send("messageRes/Device not found");
//                break;
//
//            case "turnOff":
//                DeviceSocket deviceSocket_off = Utils.getSocketDeviceByMacAddr(requests[1]);
//                if(deviceSocket_off != null){
//                    deviceSocket_off.getConn().send("changeColor/000000000000000");
//                    if(mongoDBUser.updateStatusDevice(Utils.getObjectIdBySocket(conn), requests[1], "off")){
//                        conn.send("updateStatus/" + requests[1] + "/off");
//                        conn.send("messageRes/Light turned off");
//                    }
//                }
//                else
//                    conn.send("messageRes/Device not found");
//                break;

//            case "turnOn":
//                DeviceSocket deviceSocket_on = Utils.getSocketDeviceByMacAddr(requests[1]);
//                if(deviceSocket_on != null){
//                    String currentColor = mongoDBUser.getColorByMacAddress(Utils.getObjectIdBySocket(conn), requests[1]);
//                    String currentBrightness = mongoDBUser.getBrightnessByMacAddress(Utils.getObjectIdBySocket(conn), requests[1]);
//                    //deviceSocket_on.getConn().send("turnOn/" + currentColor);
//                    deviceSocket_on.getConn().send("changeColor/255255255999255");
//                    deviceSocket_on.getConn().send("changeColor/" + currentColor + "255" + currentBrightness);
//                    //update status light of db
//                    if(mongoDBUser.updateStatusDevice(Utils.getObjectIdBySocket(conn), requests[1], "on")){
//                        conn.send("updateStatus/" + requests[1] + "/on");
//                        conn.send("messageRes/Light turned on");
//                    }
//                }
//                else
//                    conn.send("messageRes/Device not found");
//                break;

            case "changePassword":
                if (mongoDBUser.changePassword(Utils.getObjectIdBySocket(conn), requests[1], requests[2]))
                    conn.send("messageRes/Change password successfully");
                else
                    conn.send("messageRes/Change password failed");
                break;

            case "renameRoom":
                if (mongoDBUser.renameRoom(Utils.getObjectIdBySocket(conn), requests[1], requests[2]))
                    conn.send("messageRes/Rename room successfully");
                else
                    conn.send("messageRes/Rename room failed");
                break;

            case "renameDevice":
                if (mongoDBUser.renameDevice(Utils.getObjectIdBySocket(conn), requests[1], requests[2]))
                    conn.send("messageRes/Rename device successfully");
                else
                    conn.send("messageRes/Rename device failed");
                break;

            case "updateProfile":
                break;
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