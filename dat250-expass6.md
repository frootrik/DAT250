## Tutorial

I chose Kafka as my message broker in this assignment.
There is no official tutorial to go through, thus I haven't completed one.

## Implementation
The first step in this assignment was to make the necessary adjustments to the build.gradle file
and add an application.properties file, so that Spring could:

* read the configs,
* let KafkaTemplate, AdminClient, and @KafkaListener know where the broker is, and
* keep configuration out of the code (separating code from environment).

To get one Topic per Poll, I generate a slug from the poll question (lower-cased, spaces to -, unsafe chars removed) and build the topic name as:
poll-<slug>-<pollId>.
The slug keeps topic names human-readable and safe, while the UUID ensures uniqueness.

I needed to make a consumer for events, and I needed it to:

* Subscribe to all poll topics using @KafkaListener(topicPattern = "poll-.*", groupId = "pollapp").
* Parse VoteCast JSON messages (pollId, optionId, timestamp).
* update the database on each message. To support “anonymous votes” without changing my domain model, I reuse an “anonymous” user internally and persist a normal Vote. After persisting, I invalidate the poll’s cached results.

I also added topic creation on poll creation: when a new poll is persisted, I call Kafka’s AdminClient to ensure the corresponding topic exists.

Finally, I built a standalone Kotlin producer that sends a single VoteCast directly to Kafka. I run it with two command-line arguments (pollId optionId), and it publishes to the matching topic (based on the slug plus the poll’s ID). 

This proves end-to-end that:
1. a client can produce votes without going through the REST API,
2. my app consumes from Kafka, and
3. the database reflects the new vote (visible via GET /polls/{id}/results).

### What went wrong (and fixes):

1. Docker/Kafka startup confusion and CLI path differences (Bitnami vs Apache image): fixed by using the correct kafka-topics.sh path for the running image.

2. Infinite JSON recursion when returning entities (Poll ↔ User): fixed by returning lightweight DTOs from the controller.

3. “Option not found” after restarts (H2 in-memory lost data): fixed by creating fresh test data in the same run (or by switching to file-based H2).

4. Console producer syntax gotchas (missing --topic, stray quotes): fixed by using a single-line command with the proper flags.


### Testing

1. Start Kafka (docker compose up -d) and the Spring app.
2. Create a user and a poll via REST (I used curl), then fetch pollId and an optionId.
3. Run the standalone producer with those IDs.
4. Check the app logs for the consumed event and verify /polls/{id}/results increased.