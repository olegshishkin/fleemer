package com.fleemer.web.other;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.contrib.mongodb.MongoDBAccessEventAppender;
import com.mongodb.BasicDBObject;

/**
 * Class is used for substitute remote client ip while http-server is proxying all the requests. Otherwise always
 * only localhost ip will be logged.
 */
public class FleemerMongoDBAccessEventAppender extends MongoDBAccessEventAppender {
    @Override
    protected BasicDBObject toMongoDocument(IAccessEvent event) {
        BasicDBObject dbObject = super.toMongoDocument(event);
        String realIp = event.getRequestHeader("X-Forwarded-For");
        if (realIp == null || realIp.isEmpty() || realIp.equals("-")) {
            return dbObject;
        }
        BasicDBObject remote = (BasicDBObject) dbObject.get("remote");
        remote.replace("addr", realIp);
        remote.replace("host", realIp);
        return dbObject;
    }
}
