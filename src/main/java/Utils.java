import models.DeviceSocket;
import models.UserSocket;
import org.bson.types.ObjectId;
import org.java_websocket.WebSocket;

public class Utils {

    public static DeviceSocket getSocketDeviceByMacAddr(String macAddr){
        for (DeviceSocket deviceSocket : Server.deviceSocketList){
            if(deviceSocket.getMacAdrr().equals(macAddr))
                return deviceSocket;
        }
        return null;
    }
    public static ObjectId getObjectIdBySocket(WebSocket conn){
        for(UserSocket userSocket : Server.userSocketList){
            if(userSocket.getConn().equals(conn)){
                return userSocket.getId();
            }
        }
        return null;
    }

}
