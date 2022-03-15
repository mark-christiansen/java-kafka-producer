#!/bin/bash

program_name="java-kafka-producer"
version="0.0.1"

display_help() {
    echo "Usage: $0 [option...]" >&2
    echo
    echo "   -h, --help                 Displays script usage info"
    echo "   -t, --topic                The topic to produce to"
    echo "   -m, --messages             Number of messages to produce"
    echo "   -s, --schema               Name of the schema to use (without extension, must be in classpath)"
    echo "   -p, --partitions           The number of partitions to create the topic with (defaults to 1)"
    echo "   -e, --env                  The Kafka environment to connect to (application-<env>.yaml)"
    echo
    # echo some stuff here for the -a or --add-options
    exit 1
}

topic=""
messages=""
schema=""
partitions=""
env=""

# find argument names and values passed into the program
while [[ "$#" -gt 0 ]]; do
    case $1 in
        -h|--help) display_help; shift ;;
        -t|--topic) topic="$2"; shift ;;
        -m|--messages) messages="$2"; shift ;;
        -s|--schema) schema="$2"; shift ;;
        -p|--partitions) partitions="$2"; shift ;;
        -e|--env) env="$2"; shift ;;
        *) echo "Unknown parameter passed: $1" ;;
    esac
    shift
done

# verify required arguments were passed
[[ $topic == "" ]] && { echo "The option --topic (-t) is required but not set, please specify the topic name" && display_help && exit 1; }
[[ $messages == "" ]] && { echo "The option --messages (-m) is required but not set"  && display_help && exit 1; }
[[ $env == "" ]] && { echo "The option --env (-e) is required but not set, please specify the environment name"  && display_help && exit 1; }
topic="--topic $topic"
messages="--messages $messages"
if [[ $schema != "" ]]; then
  schema="--schema $schema"
fi
if [[ $partitions != "" ]]; then
  partitions="--partitions $partitions"
fi

# execute program
java -cp "$program_name-$version.jar" -Dloader.path=schemas/ -Dloader.main=com.jnj.kafka.producer.Application -Dspring.profiles.active=$env -Dspring.config.location=conf/ org.springframework.boot.loader.PropertiesLauncher $topic $messages $schema $partitions