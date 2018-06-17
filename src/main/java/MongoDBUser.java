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
