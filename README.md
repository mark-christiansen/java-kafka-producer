# java-kafka-producer
To run the script make sure that you have the compiled jar file "java-kafka-producer-0.0.1.jar" in the same directory as
the shell script "producer.sh". In that same directory create a "conf" directory and add an "application-<env>.yaml" to
that directory, where <env> is an arbitrary name you want to give the Kafka environment you are producing to. See
the example YAML file, "application-dev.yaml" in this project's "conf" folder for an example of how to setup the file.
In that YAML, all properties under "producer" and "admin" are Kafka/Confluent consumer properties as defined
[here](https://docs.confluent.io/platform/current/installation/configuration/producer-configs.html). The "admin" 
connection needs permission to create the topic, while the "producer" connection just needs permission to write to the
topic.

In addition, create a "schemas" directory in the same directory as the shell script where you will place the Avro schema
files you want to use for producing messages. The schema files should have the file extension ".avsc". The name of the
schema file should be passed to the shell script without the file extension (ex: for "schemas/company.avsc" specify 
"producer.sh --schema company").

When running the script you specify the options like show below:

```
producer.sh --topic mytopic --messages 10 --schema person --partitions 1 --env dev


    topic: the topic you want to produce to (required)
  
    messages: the number of messages you want to produce (required)
  
    schema: the name of the schema minus the file extension (.avsc) for the schema you want to use (default is "person" - built-in schema)
  
    partitions: the number of partitions to give the topic if created by this program (default is 1)
  
    env: the name of the environment (conf/application-<env>.yaml) to load
```