package app.cleancode;

import java.util.stream.IntStream;

import org.bson.Document;

import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Entrypoint {
    public static void main(String[] args) {
        System.out.println("Creating the client");
        try (var mongoClient = MongoClients.create()) {
            System.out.println("Getting the database");
            var database = mongoClient.getDatabase("test");
            System.out.println("Getting the collection");
            MongoCollection<Person> peopleCollection = database.getCollection("people", Person.class);
            System.out.println("Performing queries");
            var people = Mono.from(peopleCollection.deleteMany(new Document())).flatMap(
                    v -> Mono.from(peopleCollection.insertMany(
                            IntStream.range(0, 100).mapToObj(i -> new Person("Human", Integer.toString(i))).toList())))
                    .flatMapMany(v -> Flux.from(peopleCollection.find())).buffer(20).collectList().block();
            System.out.println("Done");
            System.out.println(people);
        }
    }
}
