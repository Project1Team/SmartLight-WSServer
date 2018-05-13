import models.DeviceSocket;
public class Utils {

    public static DeviceSocket getSocketDeviceByMacAddr(String macAddr){
        for (DeviceSocket deviceSocket : Server.deviceSocketList){
            if(deviceSocket.getMacAdrr().equals(macAddr))
                return deviceSocket;
        }
        return null;
    }

}
