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

}
