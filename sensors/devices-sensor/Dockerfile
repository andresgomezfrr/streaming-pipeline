FROM java:openjdk-8-jdk

RUN apt-get update
RUN apt-get install -y gettext-base git maven

RUN git clone https://github.com/andresgomezfrr/synthetic-producer.git
RUN cd synthetic-producer && git checkout http && mvn clean package
RUN mv /synthetic-producer/target/synthetic-producer-1.4.1-SNAPSHOT-selfcontained.jar /synthetic-producer-1.4.1-SNAPSHOT-selfcontained.jar

COPY config.yml /
COPY sensor_start.sh /

env MSG_PERSEC 100
env NUM_THREADS 1

CMD ./sensor_start.sh
