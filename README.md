# Introduction 
When sending data tokafka, sensitive value must be tokenized and detokenized. We choose to customized SMT to call API to have the value exchange with tokenization server. This Repository aims to demostrate of using customized SMT java plugin which loaded into kafka connect for kafka connectors to exchange value. 

Step: 
1.	Create new project (e.g. Net Bean Github)
2.	Add automated build system (e.g. Maven)
3.	Add Kafka Connect and Connect Transforms dependencies (to have access to Kafka Connect SMT API)
4.	Develop a simple “String Filter SMT”, e.g. by using another SMT as template, like TimestampRouter SMT for String messages
5.	Build and generate JAR (without Kafka dependencies)
6.	Add the JAR to default Connect plugin path https://docs.confluent.io/current/connect/userguide.html
7.	Start Connect via Confluent CLI
8.	Add connector including the SMT
9.	Test it
10.	Develop Unit Test


# Contribute
Reference link: 
Java with maven wouldn't build: Cannot run program “cmd” “Malformed argument has embedded quote”
I have checked the release notes for JDK 13.0.1 at https://www.oracle.com/technetwork/java/javase/13-0-1-relnotes-5592797.html#JDK-8221858
This behavior is a regression from a security fix for JDK-8221858 (not public). Follow the link for a full description. The fix is part of JDK 8u231, JDk 11.0.5, 13.0.1 etc.
To resolve this problem, append -J-Djdk.lang.Process.allowAmbiguousCommands=true to netbeans_default_options in <netbeans-dir>\etc\netbeans.conf.
