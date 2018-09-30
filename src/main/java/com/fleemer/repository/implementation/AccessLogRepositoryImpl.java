package com.fleemer.repository.implementation;

import com.fleemer.model.Person;
import com.fleemer.model.mongodb.AccessLog;
import com.fleemer.model.mongodb.AccessStats;
import com.fleemer.repository.AccessLogRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

@Repository
public class AccessLogRepositoryImpl implements AccessLogRepository {
    private static final String AGENT = "agent";
    private static final String COUNT = "count";
    private static final String HOST = "host";
    private static final String IP = "ip";
    private static final String DATE = "date";
    private static final String URI = "uri";

    private final MongoOperations mongoOperations;

    @Autowired
    public AccessLogRepositoryImpl(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    /** The final aggregation of findAll() method is as follows:
    *
    *  {
    *    "$match": {
    *      "remote.user": *USERNAME*,
    *      "$and": [{"timeStamp": {"$gte": {"$date": *FROM_DATE_INCLUDE*}, "$lte": {"$date": *TILL_DATE_INCLUDE*}}}]
    *    }
    *  },
    *  {
    *    "$project": {
    *      "ip": "$remote.addr",
    *      "host": "$remote.host",
    *      "uri": "$request.uri",
    *      "agent": "$request.userAgent",
    *      "date": {"$dateToString": {"format": "%Y-%m-01","date": "$timeStamp"}
    *      }
    *    }
    *  },
    *  {
    *    "$group": {
    *      "_id": {
    *        "date": "$date",
    *        "ip": "$ip",
    *        "host": "$host",
    *        "uri": "$uri",
    *        "agent": "$agent"
    *      },
    *      "count": {"$sum": 1}
    *    }
    *  },
    *  {
    *    "$project": {
    *      "date": "$_id.date",
    *      "ip": "$_id.ip",
    *      "host": "$_id.host",
    *      "uri": "$_id.uri",
    *      "agent": "$_id.agent",
    *      "count": 1
    *    }
    *  },
    *  {
    *    "$sort": {
    *      "date": 1,
    *      "ip": 1,
    *      "host": 1,
    *      "uri": 1,
    *      "agent": 1
    *    }
    *  }
    */
    @Override
    public List<AccessStats> findAll(Person person, LocalDate from, LocalDate till) {
        Criteria userCriteria = Criteria.where("remote.user").is(person.getEmail());
        Criteria timeCriteria = Criteria.where("timeStamp").gte(from).lt(till.plusDays(1));
        MatchOperation matchOperation = Aggregation.match(userCriteria.andOperator(timeCriteria));
        ProjectionOperation initProjectionOperation = Aggregation.project(Fields.from(
                Fields.field(IP, "remoteClient.ip"),
                Fields.field(HOST, "remoteClient.host"),
                Fields.field(URI, "request.uri"),
                Fields.field(AGENT, "request.userAgent")))
                .and("time").dateAsFormattedString("%Y-%m-01").as(DATE);
        GroupOperation groupOperation = Aggregation.group(DATE, IP, HOST, URI, AGENT).count().as(COUNT);
        ProjectionOperation finalProjectionOperation = Aggregation.project(Fields.from(
                Fields.field(DATE, "_id.date"),
                Fields.field(IP, "_id.ip"),
                Fields.field(HOST, "_id.host"),
                Fields.field(URI, "_id.uri"),
                Fields.field(AGENT, "_id.agent"),
                Fields.field(COUNT)));
        SortOperation sortOperation = Aggregation.sort(Sort.Direction.ASC, DATE, IP, HOST, URI, AGENT);
        TypedAggregation<AccessLog> aggregation = Aggregation.newAggregation(AccessLog.class,
                matchOperation,
                initProjectionOperation,
                groupOperation,
                finalProjectionOperation,
                sortOperation);
        AggregationResults<AccessStats> results = mongoOperations.aggregate(aggregation, AccessStats.class);
        return results.getMappedResults();
    }
}
