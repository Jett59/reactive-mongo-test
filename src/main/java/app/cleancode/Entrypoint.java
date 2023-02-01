package app.cleancode;

import java.util.List;
import java.util.stream.IntStream;

import org.bson.Document;

import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Entrypoint {
    public static void main(String[] args) {
        try (var mongoClient = MongoClients.create()) {
            var database = mongoClient.getDatabase("test");
            var peopleCollection = database.getCollection("people", Person.class);

            List<Person> people = IntStream
                    .range(0, 100)
                    .mapToObj(i -> new Person("Human", Integer.toString(i)))
                    .toList();

            Mono.from(peopleCollection.deleteMany(new Document()))
                    .flatMap(v -> Mono.from(peopleCollection.insertMany(people)))
                    .flatMapMany(v -> Flux.from(peopleCollection.find()))
                    .buffer(20)
                    .doOnNext(System.out::println) // Here's where you might write the results to a chunked web service response 20 at a time
                    .blockLast();

            System.out.println("Done");
        }
    }
}
