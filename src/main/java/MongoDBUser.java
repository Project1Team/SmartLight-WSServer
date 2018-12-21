import com.mongodb.*;

import java.util.List;

import models.UserSocket;
import org.bson.types.ObjectId;

public class MongoDBUser {
    private DBCollection collection;

    public MongoDBUser(DBCollection collection) {
        this.collection = collection;
    }

    public int checkLoginUser(String userName, String password, List<UserSocket> userSockets){
        DBObject query = BasicDBObjectBuilder.start().append("name", userName).append("password", password).get();
        int count = this.collection.find(query).length();
        if(count != 0) {
            ObjectId idAccount = (ObjectId) getUserDBObject(userName, password).get("_id");
            for (UserSocket userSocket : userSockets) {
                if (userSocket.getId().equals(idAccount))
                    return 2;
            }
            return 1;
        }
        return 0;
    }

    public DBObject getUserDBObject(String userName, String password){
        DBObject query = BasicDBObjectBuilder.start().append("name", userName).append("password", password).get();
        DBCursor cursor = this.collection.find(query);
        return cursor.next();
    }

    public DBObject getUserDBObjectById(ObjectId objectId){
        DBObject query = BasicDBObjectBuilder.start().append("_id", objectId).get();
        DBCursor cursor = this.collection.find(query);
        return cursor.next();
    }

    public String getCurrentColorByMacAddress(String macAddr){
        DBCursor cursor_user = collection.find();
        while (cursor_user.hasNext()){
            DBObject dbobject_user = cursor_user.next();
            BasicDBList basicDBList_room = (BasicDBList) dbobject_user.get("room");
            for(Object object_room : basicDBList_room){
                BasicDBList basicDBList_device = (BasicDBList) ((DBObject) object_room).get("device");
                for(Object object_device: basicDBList_device){
                    if(((DBObject) object_device).get("macAddr").equals(macAddr))
                        return (String) ((DBObject) object_device).get("color");
                }
            }
        }
        return null;
    }
    public String getCurrentBrightnessByMacAddr(String macAddr){
        DBCursor cursor_user = collection.find();
        while (cursor_user.hasNext()){
            DBObject dbobject_user = cursor_user.next();
            BasicDBList basicDBList_room = (BasicDBList) dbobject_user.get("room");
            for(Object object_room : basicDBList_room){
                BasicDBList basicDBList_device = (BasicDBList) ((DBObject) object_room).get("device");
                for(Object object_device: basicDBList_device){
                    if(((DBObject) object_device).get("macAddr").equals(macAddr))
                        return (String) ((DBObject) object_device).get("brightness");
                }
            }
        }
        return null;
    }
    public String getCurrentStatusByMacAddr(String macAddr){
        DBCursor cursor_user = collection.find();
        while (cursor_user.hasNext()){
            DBObject dbobject_user = cursor_user.next();
            BasicDBList basicDBList_room = (BasicDBList) dbobject_user.get("room");
            for(Object object_room : basicDBList_room){
                BasicDBList basicDBList_device = (BasicDBList) ((DBObject) object_room).get("device");
                for(Object object_device: basicDBList_device){
                    if(((DBObject) object_device).get("macAddr").equals(macAddr))
                        return (String) ((DBObject) object_device).get("status");
                }
            }
        }
        return null;
    }
    public String getBrightnessByMacAddress(ObjectId idUser, String macAddr){
        DBObject dbObject = getUserDBObjectById(idUser);
        BasicDBList basicDBList_room = (BasicDBList) dbObject.get("room");
        for(Object object_room : basicDBList_room){
            BasicDBList basicDBList_device = (BasicDBList) ((DBObject) object_room).get("device");
            for(Object object_device: basicDBList_device){
                if(((DBObject) object_device).get("macAddr").equals(macAddr))
                    return (String) ((DBObject) object_device).get("brightness");
            }
        }
        return null;
    }
    public String getColorByMacAddress(ObjectId idUser, String macAddr){
        DBObject dbObject = getUserDBObjectById(idUser);
        BasicDBList basicDBList_room = (BasicDBList) dbObject.get("room");
        for(Object object_room : basicDBList_room){
            BasicDBList basicDBList_device = (BasicDBList) ((DBObject) object_room).get("device");
            for(Object object_device: basicDBList_device){
                if(((DBObject) object_device).get("macAddr").equals(macAddr))
                    return (String) ((DBObject) object_device).get("color");
            }
        }
        return null;
    }

    public boolean updateColorDb(ObjectId idUser, String macAddress, String colorUpdate){
        int position_room = 0;
        int position_device = 0;
        boolean alert_found = false;

        BasicDBObject queryUser = new BasicDBObject();
        BasicDBObject dataUpdate = new BasicDBObject();
        BasicDBObject command = new BasicDBObject();

        DBObject dbObject = getUserDBObjectById(idUser);
        BasicDBList basicDBList_room = (BasicDBList) dbObject.get("room");

        findPostionOfRoomDevice:{
            for(Object object_room : basicDBList_room){
                BasicDBList basicDBList_device = (BasicDBList) ((DBObject) object_room).get("device");
                for(Object object_device: basicDBList_device){
                    if(((DBObject) object_device).get("macAddr").equals(macAddress)) {
                        alert_found = true;
                        break findPostionOfRoomDevice;
                    }
                    position_device++;
                }
                position_room ++;
            }
        }
        if (alert_found){
            queryUser.put("_id",idUser);
            dataUpdate.put("room." + position_room + ".device." + position_device + ".color", colorUpdate);
            command.put("$set", dataUpdate);
            collection.update(queryUser, command);
            return true;
        }
        return false;
    }
    public boolean updateBrightnessDb(ObjectId idUser, String macAddress, String brightnessUpdate){
        int position_room = 0;
        int position_device = 0;
        boolean alert_found = false;

        BasicDBObject queryUser = new BasicDBObject();
        BasicDBObject dataUpdate = new BasicDBObject();
        BasicDBObject command = new BasicDBObject();

        DBObject dbObject = getUserDBObjectById(idUser);
        BasicDBList basicDBList_room = (BasicDBList) dbObject.get("room");

        findPostionOfRoomDevice:{
            for(Object object_room : basicDBList_room){
                BasicDBList basicDBList_device = (BasicDBList) ((DBObject) object_room).get("device");
                for(Object object_device: basicDBList_device){
                    if(((DBObject) object_device).get("macAddr").equals(macAddress)) {
                        alert_found = true;
                        break findPostionOfRoomDevice;
                    }
                    position_device++;
                }
                position_room ++;
            }
        }
        if (alert_found){
            queryUser.put("_id",idUser);
            dataUpdate.put("room." + position_room + ".device." + position_device + ".brightness", brightnessUpdate);
            command.put("$set", dataUpdate);
            collection.update(queryUser, command);
            return true;
        }
        return false;
    }

    public boolean updateStatusDevice(ObjectId idUser, String macAddress, String status){
        int position_room = 0;
        int position_device = 0;
        boolean alert_found = false;

        BasicDBObject queryUser = new BasicDBObject();
        BasicDBObject dataUpdate = new BasicDBObject();
        BasicDBObject command = new BasicDBObject();

        DBObject dbObject = getUserDBObjectById(idUser);
        BasicDBList basicDBList_room = (BasicDBList) dbObject.get("room");

        findPostionOfRoomDevice:{
            for(Object object_room : basicDBList_room){
                BasicDBList basicDBList_device = (BasicDBList) ((DBObject) object_room).get("device");
                for(Object object_device: basicDBList_device){
                    if(((DBObject) object_device).get("macAddr").equals(macAddress)) {
                        alert_found = true;
                        break findPostionOfRoomDevice;
                    }
                    position_device++;
                }
                position_room ++;
            }
        }
        if (alert_found){
            queryUser.put("_id",idUser);
            dataUpdate.put("room." + position_room + ".device." + position_device + ".status", status);
            command.put("$set", dataUpdate);
            collection.update(queryUser, command);
            return true;
        }
        return false;
    }

    public boolean updateTemperate(ObjectId idUser,  String macAddr, String temperate){
        int position_home = 0;
        int position_device = 0;
        boolean alert_found = false;

        BasicDBObject queryUser = new BasicDBObject();
        BasicDBObject dataUpdate = new BasicDBObject();
        BasicDBObject command = new BasicDBObject();

        DBObject dbObject = getUserDBObjectById(idUser);
        BasicDBList basicDBList_home = (BasicDBList) dbObject.get("home");

        findPostionOfHomeDevice:{
            for(int i = 0; i < basicDBList_home.size(); i++){
                Object object_home = basicDBList_home.get(i);
                if(((DBObject) object_home).get("macAddr").equals(macAddr)) {
                    BasicDBList basicDBList_device = (BasicDBList) ((DBObject) object_home).get("device");
                    for(int j = 0; j < basicDBList_device.size(); j++){
                        Object object_teamperature = basicDBList_device.get(j);
                        if(((DBObject) object_teamperature).get("type").equals("temperature")){
                            alert_found = true;
                            position_device = j;
                            break findPostionOfHomeDevice;
                        }
                    }
                }
            }
            position_home++;
        }
        if (alert_found){
            queryUser.put("_id",idUser);
            dataUpdate.put("home." + position_home + ".device." + position_device + ".value", temperate);
            command.put("$set", dataUpdate);
            collection.update(queryUser, command);
            return true;
        }
        return false;
    }

    public boolean updateFire(ObjectId idUser,  String macAddr, String fire){
        int position_home = 0;
        int position_device = 0;
        boolean alert_found = false;

        BasicDBObject queryUser = new BasicDBObject();
        BasicDBObject dataUpdate = new BasicDBObject();
        BasicDBObject command = new BasicDBObject();

        DBObject dbObject = getUserDBObjectById(idUser);
        BasicDBList basicDBList_home = (BasicDBList) dbObject.get("home");
        findPostionOfHomeDevice:{
            for(int i = 0; i < basicDBList_home.size(); i++){
                Object object_home = basicDBList_home.get(i);
                if(((DBObject) object_home).get("macAddr").equals(macAddr)) {
                    BasicDBList basicDBList_device = (BasicDBList) ((DBObject) object_home).get("device");
                    for(int j = 0; j < basicDBList_device.size(); j++){
                        Object object_fire = basicDBList_device.get(j);
                        if(((DBObject) object_fire).get("type").equals("fire")){
                            alert_found = true;
                            position_device = j;
                            break findPostionOfHomeDevice;
                        }
                    }
                }
            }
            position_home++;
        }
        if (alert_found){
            queryUser.put("_id",idUser);
            dataUpdate.put("home." + position_home + ".device." + position_device + ".status", fire);
            command.put("$set", dataUpdate);
            collection.update(queryUser, command);
            return true;
        }
        return false;
    }

    public boolean updateGas(ObjectId idUser,  String macAddr, String gas){
        int position_home = 0;
        int position_device = 0;
        boolean alert_found = false;

        BasicDBObject queryUser = new BasicDBObject();
        BasicDBObject dataUpdate = new BasicDBObject();
        BasicDBObject command = new BasicDBObject();

        DBObject dbObject = getUserDBObjectById(idUser);
        BasicDBList basicDBList_home = (BasicDBList) dbObject.get("home");

        findPostionOfHomeDevice:{
            for(int i = 0; i < basicDBList_home.size(); i++){
                Object object_home = basicDBList_home.get(i);
                if(((DBObject) object_home).get("macAddr").equals(macAddr)) {
                    BasicDBList basicDBList_device = (BasicDBList) ((DBObject) object_home).get("device");
                    for(int j = 0; j < basicDBList_device.size(); j++){
                        Object object_gas = basicDBList_device.get(j);
                        if(((DBObject) object_gas).get("type").equals("gas")){
                            alert_found = true;
                            position_device = j;
                            break findPostionOfHomeDevice;
                        }
                    }
                }
            }
            position_home++;
        }
        if (alert_found){
            queryUser.put("_id",idUser);
            dataUpdate.put("home." + position_home + ".device." + position_device + ".status", gas);
            command.put("$set", dataUpdate);
            collection.update(queryUser, command);
            return true;
        }
        return false;
    }

    public boolean updateStatusSwitch(ObjectId idUser, String macAddress, String value){
        int position_home = 0;
        int position_switch = 0;
        int position_device = 0;
        String index_switch = "0";
        String status = "off";
        boolean alert_found = false;

        BasicDBObject queryUser = new BasicDBObject();
        BasicDBObject dataUpdate = new BasicDBObject();
        BasicDBObject command = new BasicDBObject();

        switch (value){
            case "010":
                index_switch = "1";
                position_switch = 0;
                status = "off";
                break;
            case "011":
                index_switch = "1";
                position_switch = 0;
                status = "on";
                break;
            case "020":
                index_switch = "1";
                position_switch = 1;
                status = "off";
                break;
            case "021":
                index_switch = "1";
                position_switch = 1;
                status = "on";
                break;
            case "030":
                index_switch = "1";
                position_switch = 2;
                status = "off";
                break;
            case "031":
                index_switch = "1";
                position_switch = 2;
                status = "on";
                break;
            case "040":
                index_switch = "1";
                position_switch = 3;
                status = "off";
                break;
            case "041":
                index_switch = "1";
                position_switch = 3;
                status = "on";
                break;
            case "110":
                index_switch = "2";
                position_switch = 0;
                status = "off";
                break;
            case "111":
                index_switch = "2";
                position_switch = 0;
                status = "on";
                break;
            case "120":
                index_switch = "2";
                position_switch = 1;
                status = "off";
                break;
            case "121":
                index_switch = "2";
                position_switch = 1;
                status = "on";
                break;
            case "130":
                index_switch = "2";
                position_switch = 2;
                status = "off";
                break;
            case "131":
                index_switch = "2";
                position_switch = 2;
                status = "on";
                break;
            case "140":
                index_switch = "2";
                position_switch = 3;
                status = "off";
                break;
            case "141":
                index_switch = "2";
                position_switch = 3;
                status = "on";
                break;
        }
        DBObject dbObject = getUserDBObjectById(idUser);
        BasicDBList basicDBList_home = (BasicDBList) dbObject.get("home");

        findPostionOfHomeDevice:{
            for(int i = 0; i < basicDBList_home.size(); i++){
                Object object_home = basicDBList_home.get(i);
                if(((DBObject) object_home).get("macAddr").equals(macAddress)) {
                    BasicDBList basicDBList_device = (BasicDBList) ((DBObject) object_home).get("device");
                    for(int j = 0; j < basicDBList_device.size(); j++){
                        Object object_switchBoard = basicDBList_device.get(j);
                        if(((DBObject) object_switchBoard).get("type").equals("switch")){
                            if(((DBObject) object_switchBoard).get("index").equals(index_switch)){
                                alert_found = true;
                                position_device = j;
                                break findPostionOfHomeDevice;
                            }
                        }
                    }
                }
            }
            position_home++;
        }
        if (alert_found){
            queryUser.put("_id",idUser);
            dataUpdate.put("home." + position_home + ".device." + position_device + ".switch." + position_switch + ".status", status);
            command.put("$set", dataUpdate);
            collection.update(queryUser, command);
            return true;
        }
        return false;
    }

    public boolean changePassword(ObjectId idUser, String passOld, String newPass){
        if(getUserDBObjectById(idUser).get("password").equals(passOld)){
            BasicDBObject queryUser = new BasicDBObject();
            BasicDBObject passUpdate = new BasicDBObject();
            BasicDBObject command = new BasicDBObject();

            queryUser.put("_id", idUser);
            passUpdate.put("password", newPass);

            command.put("$set",passUpdate);
            collection.update(queryUser, command);

            return true;
        }
        return false;
    }

    public boolean renameRoom(ObjectId idUser, String oldName, String newName){
        int position_room = 0;
        boolean alert_found = false;
        BasicDBObject queryUser = new BasicDBObject();
        BasicDBObject nameUpdate = new BasicDBObject();
        BasicDBObject command = new BasicDBObject();

        DBObject dbObject = getUserDBObjectById(idUser);
        BasicDBList basicDBList_room = (BasicDBList) dbObject.get("room");

        for(Object object_room : basicDBList_room){
            if (((DBObject)object_room).get("name").equals(oldName)){
                alert_found = true;
                break;
            }
            position_room ++;
        }
        if (alert_found) {
            queryUser.put("_id", idUser);
            nameUpdate.put("room." + position_room + ".name", newName);
            command.put("$set", nameUpdate);
            collection.update(queryUser, command);
            return true;
        }
        return false;
    }

    public boolean renameDevice(ObjectId idUser, String macAddress, String newName){
        int position_room = 0;
        int position_device = 0;
        boolean alert_found = false;

        BasicDBObject queryUser = new BasicDBObject();
        BasicDBObject nameUpdate = new BasicDBObject();
        BasicDBObject command = new BasicDBObject();

        DBObject dbObject = getUserDBObjectById(idUser);
        BasicDBList basicDBList_room = (BasicDBList) dbObject.get("room");

        findPostionOfRoomDevice:{
            for(Object object_room : basicDBList_room){
                BasicDBList basicDBList_device = (BasicDBList) ((DBObject) object_room).get("device");
                for(Object object_device: basicDBList_device){
                    if(((DBObject) object_device).get("macAddr").equals(macAddress)) {
                        alert_found = true;
                        break findPostionOfRoomDevice;
                    }
                    position_device++;
                }
                position_room ++;
            }
        }
        if (alert_found){
            queryUser.put("_id",idUser);
            nameUpdate.put("room." + position_room + ".device." + position_device + ".name", newName);
            command.put("$set", nameUpdate);
            collection.update(queryUser, command);
            return true;
        }
        return false;
    }

}
