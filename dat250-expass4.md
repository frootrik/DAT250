
The first thing I struggled with in this assignment was getting the imports right, and after adding the dependencies, they were unresolved references. I did not know that Gradle needed to be synced after adding dependencies, but after I did that, it was ok. 

After adding the dependencies and adjusting the import statements to fit my particular project structure, I started resolving the compilation issues. I had some hard-coded test cases that I used in the last assignment that I needed to get rid of, and after I started the transition from a in-memory model to a JPA entity model, I had to make some decisions about IDs in the data classes. Then, I realized that I had to refactor these to work with entities, which made many changes necessary. 

