
In this assignment, I experimented as instructed with Redis in the terminal to make the sequence

Initial state: no user is logged in
User "alice" logs in
User "bob" logs in
User "alice" logs off
User "eve" logs in

work in Redis, which I did like this:

> sadd online “alice”
> sadd online “bob”
> smembers online – “alice”, “bob”
> srem online “alice”
> sadd online “eve”
> smembers online – “bob”, “eve”

Which worked fine. Then I made a Main method in my project to replicate this in Kotlin, which also worked fine. 
Then I struggled for a while with the Cache assignment, which I'm not sure I understood correctly, and when
I started writing the report I saw that I should write about the use of MongoDB, and I wonder if this is a typo? Nothing
else had anything to do with MongoDB this assignment, so in order to implement the cache, I assumed I would use my
backend server on port 8080 to be my relational or persistant storage, and that I'd make Redis work like a cache by adding it
as a data class in my project, along with a config file and the necessary changes to the PollManager and Controller. 

I saw this MongoDB addition too late, so if I have misunderstood something, it's too late for me to fix right now anyway.
I also saw too late that the deadline is moved forward half an hour, so I do not have time to elaborate further, but
if that is required I will do so. 