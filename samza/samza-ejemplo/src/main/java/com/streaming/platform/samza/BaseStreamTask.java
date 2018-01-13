package com.streaming.platform.samza;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.samza.config.Config;
import org.apache.samza.storage.kv.KeyValueStore;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class BaseStreamTask implements StreamTask, InitableTask {
    private static final Logger log = LoggerFactory.getLogger(BaseStreamTask.class);

    private final SystemStream OUTPUT_SENSOR_CONTROL = new SystemStream("kafka", "sensor-control");
    private final SystemStream OUTPUT_SENSOR_SYSTEM_METRICS = new SystemStream("kafka", "sensor-system-metrics");
    private final SystemStream OUTPUT_SENSOR_DATA_METRICS = new SystemStream("kafka", "sensor-data-metrics");

    KeyValueStore<String, Map<String, Object>> store;
    String taskName;
    String hostname = "unknown";
    final Integer MINUTES_10 = 600;

    @Override
    public void init(Config config, TaskContext context) {
        store = (KeyValueStore<String, Map<String, Object>>) context.getStore("sensordata");
        taskName = context.getTaskName().getTaskName();

        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        } catch (UnknownHostException ex) {
            System.out.println("Hostname can not be resolved");
        }
    }

    public void process(IncomingMessageEnvelope envelope,
                        MessageCollector collector,
                        TaskCoordinator coordinator) {
        String key = (String) envelope.getKey();
        Map<String, Object> message = (Map<String, Object>) envelope.getMessage();

        String stream = envelope.getSystemStreamPartition().getSystemStream().getStream();

        if (stream.equals("sensor-system")) {
            // {"timestamp":1515413088,"monitor":"load_15","value":"0.000000","sensor_name":"my-sensor-1","type":"system","unit":"load"}
            Map<String, Object> message2send = new HashedMap<>();
            message2send.put("timestamp", message.get("timestamp"));
            message2send.put("type", message.get("monitor"));
            message2send.put("value", message.get("value"));
            message2send.put("sensor", message.get("sensor_name"));

            log.info("Sending sensor system message {}", message2send);
            collector.send(new OutgoingMessageEnvelope(OUTPUT_SENSOR_SYSTEM_METRICS, message2send));
        } else {
            if (envelope.getKey() != null) {
                partitioned(stream, key, message, collector);
            } else {
                toPartitioner(stream, message, collector);
            }
        }
    }

    private void partitioned(String stream, String key, Map<String, Object> message, MessageCollector collector) {
        if (stream.equals("sensor-iot-partitioned")) {
            Map<String, Object> message2store = new HashMap<>();
            message2store.put("temperature", message.get("temperature"));
            message2store.put("humidity", message.get("humidity"));
            message2store.put("timestamp", message.get("timestamp"));
            store.put(key, message2store);

            Map<String, Object> message2send = new HashMap<>(message2store);
            message2send.put("sensor", message.get("sensor_id"));

            log.info("Sending sensor data message {}", message2send);
            collector.send(new OutgoingMessageEnvelope(
                    OUTPUT_SENSOR_DATA_METRICS,
                    key,
                    message2send
            ));
        } else if (stream.equals("sensor-devices-partitioned")) {
            Map<String, Object> sensorData = store.get(key);
            Long timestamp = ((Number) message.get("timestamp")).longValue();

            Map<String, Object> message2send = new HashMap<>();

            message2send.put("timestamp", timestamp);
            message2send.put("device", message.get("device"));
            message2send.put("sensor", key);
            message2send.put("rssi", message.get("rssi"));

            if (sensorData != null) {
                Long lastTimestamp = ((Number) sensorData.get("timestamp")).longValue();

                Boolean outOfTime = timestamp - lastTimestamp > MINUTES_10;

                if (!outOfTime) {
                    message2send.put("temperature", sensorData.get("temperature"));
                    message2send.put("humidity", sensorData.get("humidity"));

                    log.info("Sending sensor data message {}", message2send);
                    collector.send(new OutgoingMessageEnvelope(
                            OUTPUT_SENSOR_DATA_METRICS,
                            key,
                            message2send
                    ));
                } else {
                    log.info("Cache timeout, removing data from sensor [{}]", key);
                    store.delete(key);
                }
            } else {
                log.info("Sending sensor data message {}", message2send);
                collector.send(new OutgoingMessageEnvelope(
                        OUTPUT_SENSOR_DATA_METRICS,
                        key,
                        message2send
                ));
            }
        }
    }

    private void toPartitioner(String stream, Map<String, Object> message, MessageCollector collector) {
        String newKey = "";
        Map<String, Object> message2send = new HashMap<>();

        if (stream.equals("sensor-iot")) {
            // {"sensor_id":"sensor1","timestamp":1515416700,"temperature":24,"humidity":56}
            newKey = (String) message.get("sensor_id");
            message2send.putAll(message);
            generateControlMessage(message, collector);
        } else if (stream.equals("sensor-devices")) {
            // {"timestamp":1515429602,"id":"sensor1","device":"00:00:00:00:00:2F","rssi":-44}
            newKey = (String) message.get("id");
            message2send.putAll(message);
        }

        collector.send(new OutgoingMessageEnvelope(
                new SystemStream("kafka", String.format("%s-partitioned", stream)),
                newKey,
                message2send
        ));
    }

    private void generateControlMessage(Map<String, Object> message, MessageCollector collector) {
        Integer temperature = (Integer) message.get("temperature");

        Map<String, Object> message2send = new HashMap<>();

        if (temperature > 26) {
            message2send.put("sensor", message.get("sensor_id"));
            message2send.put("action", "off");
        } else if (temperature < 21) {
            message2send.put("sensor", message.get("sensor_id"));
            message2send.put("action", "on");
        }

        if (!message2send.isEmpty()) {
            log.info("Sending control message {}", message2send);
            collector.send(new OutgoingMessageEnvelope(
                    OUTPUT_SENSOR_CONTROL,
                    message2send
            ));
        }

    }
}