#!/usr/bin/env bash
java -jar /synthetic-producer-1.4.1-SNAPSHOT-selfcontained.jar -c /config.yml -e ${ENDPOINT} -r ${MSG_PERSEC} -t ${NUM_THREADS}
