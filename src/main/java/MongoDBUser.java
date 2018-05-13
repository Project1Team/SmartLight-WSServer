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
}
