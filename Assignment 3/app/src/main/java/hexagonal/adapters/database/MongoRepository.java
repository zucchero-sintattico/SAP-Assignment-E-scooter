package hexagonal.adapters.database;

import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import hexagonal.adapters.impl.RideSerializerImpl;
import hexagonal.domain.entities.Ride;
import hexagonal.domain.repositories.IRideRepository;

import org.bson.Document;
import java.util.Optional;
import io.vertx.core.Future;

public class MongoRepository implements IRideRepository {
    private final MongoCollection<Document> collection;
    private final RideSerializerImpl rideSerializer;

    public MongoRepository(String connectionString, String dbName, String collectionName, RideSerializerImpl rideSerializer) {
        this.collection = MongoClients.create(connectionString)
                    .getDatabase(dbName)
                    .getCollection(collectionName);
        this.rideSerializer = rideSerializer;
    }

    @Override
    public Future<Void> save(Ride ride) {
        Document doc = Document.parse(rideSerializer.serialize(ride));
        String id = ride.getId(); // Assuming Ride has a getId() method

        return Future.future(promise -> {
            collection.replaceOne(new Document("id", id), doc, new ReplaceOptions().upsert(true))
                    .subscribe(new Subscriber<UpdateResult>() {
                        @Override
                        public void onSubscribe(Subscription s) {
                            s.request(Long.MAX_VALUE);
                        }

                        @Override
                        public void onNext(UpdateResult updateResult) {
                            System.out.println("Document saved with id: " + id);
                            promise.complete();
                        }

                        @Override
                        public void onError(Throwable t) {
                            System.err.println("Error saving document: " + t.getMessage());
                            promise.fail(t);
                        }

                        @Override
                        public void onComplete() {
                            // No-op
                        }
                    });
        });
    }

    @Override
    public Future<Optional<Ride>> findRideById(String id) {
        return Future.future(promise -> {
            collection.find(new Document("id", id)).first().subscribe(new Subscriber<Document>() {
                private Document doc;

                @Override
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                @Override
                public void onNext(Document document) {
                    this.doc = document;
                    System.out.println("Document found with id: " + id);
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Error finding document: " + t.getMessage());
                    promise.fail(t);
                }

                @Override
                public void onComplete() {
                    if (doc == null) {
                        System.out.println("No document found with id: " + id);
                        promise.complete(Optional.empty());
                    } else {
                        promise.complete(Optional.of(rideSerializer.deserialize(doc.toJson())));
                    }
                }
            });
        });
    }

    @Override
    public Future<Integer> getNumberOfOnGoingRides() {
        return Future.future(promise -> {
            collection.countDocuments(new Document("onGoing", true)).subscribe(new Subscriber<Long>() {
                private Long count;

                @Override
                public void onSubscribe(Subscription s) {
                    s.request(1);
                }

                @Override
                public void onNext(Long aLong) {
                    this.count = aLong;
                }

                @Override
                public void onError(Throwable t) {
                    promise.fail(t);
                }

                @Override
                public void onComplete() {
                    promise.complete(count.intValue());
                }
            });
        });
    }
}
