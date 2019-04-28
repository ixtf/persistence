package gx_3000;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ixtf.persistence.mongo.Jmongo;
import lombok.SneakyThrows;
import org.bson.conversions.Bson;
import orm.domain.Silk;

import java.util.Optional;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.mongodb.client.model.Filters.eq;

/**
 * @author jzb 2019-04-10
 */
public class Test {

    @SneakyThrows
    public static void main(String[] args) {
        String s = "{\"spindle\":[{\"spindleCode\":\"19032601929I\",\"weight\":\"0\"},{\"spindleCode\":\"19032601929G\",\"weight\":\"0\"},{\"spindleCode\":\"19032601942G\",\"weight\":\"0\"},{\"spindleCode\":\"19032601934D\",\"weight\":\"0\"},{\"spindleCode\":\"19032601934F\",\"weight\":\"0\"},{\"spindleCode\":\"19032601929N\",\"weight\":\"0\"},{\"spindleCode\":\"19032601929L\",\"weight\":\"0\"},{\"spindleCode\":\"19032601929P\",\"weight\":\"0\"},{\"spindleCode\":\"19032601934E\",\"weight\":\"0\"},{\"spindleCode\":\"19032601934C\",\"weight\":\"0\"},{\"spindleCode\":\"19032601929O\",\"weight\":\"0\"},{\"spindleCode\":\"19032601929M\",\"weight\":\"0\"},{\"spindleCode\":\"19032601944D\",\"weight\":\"0\"},{\"spindleCode\":\"19032601944B\",\"weight\":\"0\"},{\"spindleCode\":\"19032601944F\",\"weight\":\"0\"},{\"spindleCode\":\"19032601953J\",\"weight\":\"0\"},{\"spindleCode\":\"19032601953H\",\"weight\":\"0\"},{\"spindleCode\":\"19032601953L\",\"weight\":\"0\"},{\"spindleCode\":\"19032601944E\",\"weight\":\"0\"},{\"spindleCode\":\"19032601944C\",\"weight\":\"0\"},{\"spindleCode\":\"19032601953K\",\"weight\":\"0\"},{\"spindleCode\":\"19032601953I\",\"weight\":\"0\"},{\"spindleCode\":\"19032601943Y\",\"weight\":\"0\"},{\"spindleCode\":\"19032601943W\",\"weight\":\"0\"},{\"spindleCode\":\"19032601944A\",\"weight\":\"0\"},{\"spindleCode\":\"19032601953E\",\"weight\":\"0\"},{\"spindleCode\":\"19032601953C\",\"weight\":\"0\"},{\"spindleCode\":\"19032601953G\",\"weight\":\"0\"},{\"spindleCode\":\"19032601943Z\",\"weight\":\"0\"},{\"spindleCode\":\"19032601943X\",\"weight\":\"0\"},{\"spindleCode\":\"19032601953F\",\"weight\":\"0\"},{\"spindleCode\":\"19032601953D\",\"weight\":\"0\"},{\"spindleCode\":\"19032601948Y\",\"weight\":\"0\"},{\"spindleCode\":\"19032601948W\",\"weight\":\"0\"},{\"spindleCode\":\"19032601949A\",\"weight\":\"0\"},{\"spindleCode\":\"19032601939S\",\"weight\":\"0\"},{\"spindleCode\":\"19032601939Q\",\"weight\":\"0\"},{\"spindleCode\":\"19032601939U\",\"weight\":\"0\"},{\"spindleCode\":\"19032601948Z\",\"weight\":\"0\"},{\"spindleCode\":\"19032601948X\",\"weight\":\"0\"},{\"spindleCode\":\"19032601939T\",\"weight\":\"0\"},{\"spindleCode\":\"19032601939R\",\"weight\":\"0\"}],\"boxCode\":\"010120190328GC0415111C1132\",\"netWeight\":\"630\",\"grossWeight\":\"662.95\",\"grade\":\"AA\",\"automaticPackeLine\":\"1\",\"classno\":\"3\",\"palletCode\":\"00000000\"}";
        final JsonNode jsonNode = MAPPER.readTree(s);
        for (JsonNode spindleNode : jsonNode.get("spindle")) {
            final String spindleCode = spindleNode.get("spindleCode").asText();
            final Bson condition = eq("code", spindleCode);
            final Optional<Silk> optionalSilk = Jmongo.query(Silk.class, condition, 0, 1).findFirst().run().toCompletableFuture().get();
            if (optionalSilk.isEmpty()) {
                System.out.println(spindleCode);
            }
        }
    }
}
