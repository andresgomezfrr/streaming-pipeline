package com.kschool.samza;

import org.apache.samza.config.Config;
import org.apache.samza.storage.kv.Entry;
import org.apache.samza.storage.kv.KeyValueIterator;
import org.apache.samza.storage.kv.KeyValueStore;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseStreamTask implements StreamTask, InitableTask, WindowableTask {
    private static final Logger log = LoggerFactory.getLogger(BaseStreamTask.class);

    private final SystemStream OUTPUT_STREAM = new SystemStream("kafka", "data_post");
    private final SystemStream OUTPUT_STREAM_MID = new SystemStream("kafka", "data_mid");
    private final SystemStream OUTPUT_STREAM_MONITOR = new SystemStream("kafka", "monitor");
    KeyValueStore<String, Map<String, Object>> store;
    String taskName;
    Long count = 0L;
    String hostname = "Unknown";

    @Override
    public void init(Config config, TaskContext context) throws Exception {
        store = (KeyValueStore<String, Map<String, Object>>) context.getStore("sensordata");
        taskName = context.getTaskName().getTaskName();

        try
        {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostname = addr.getHostName();
        }
        catch (UnknownHostException ex)
        {
            System.out.println("Hostname can not be resolved");
        }
    }

    public void process(IncomingMessageEnvelope envelope,
                        MessageCollector collector,
                        TaskCoordinator coordinator) {
        if (envelope.getSystemStreamPartition().getSystemStream().getStream().equals("data_mid")) {
            String sensorName = (String) envelope.getKey();
            Map<String, Object> data = (Map<String, Object>) envelope.getMessage();

            Map<String, Object> cacheData = store.get(sensorName);

            if (cacheData == null) {
                Map<String, Object> newData = new HashMap<>();
                newData.put((String) data.get("monitor"), data.get("value"));
                store.put(sensorName, newData);
            } else {
                Map<String, Object> mergeData = new HashMap<>();
                mergeData.putAll(cacheData);
                mergeData.put((String) data.get("monitor"), data.get("value"));
                store.put(sensorName, mergeData);
            }
        } else if (envelope.getSystemStreamPartition().getSystemStream().getStream().equals("data")) {
            count++;
            collector.send(new OutgoingMessageEnvelope(OUTPUT_STREAM_MID, envelope.getKey(), envelope.getMessage()));
        }
    }

    @Override
    public void window(MessageCollector collector, TaskCoordinator coordinator) throws Exception {
        KeyValueIterator<String, Map<String, Object>> iter = store.all();
        List<String> keys = new ArrayList<>();

        while (iter.hasNext()) {
            Entry<String, Map<String, Object>> entry = iter.next();
            Map<String, Object> toSend = new HashMap<>();

            toSend.put("sensor_name", entry.getKey());
            toSend.putAll(entry.getValue());
            toSend.put("timestamp", System.currentTimeMillis() / 1000L);
            toSend.put("taskName", taskName);

            keys.add(entry.getKey());
            collector.send(new OutgoingMessageEnvelope(OUTPUT_STREAM, entry.getKey(), toSend));
        }

        store.deleteAll(keys);

        Map<String, Object> monitor = new HashMap<>();
        monitor.put("taskName", taskName);
        monitor.put("hostname", hostname);
        monitor.put("timestamp", System.currentTimeMillis() / 1000L);
        monitor.put("msgSec", count / 60);
        collector.send(new OutgoingMessageEnvelope(OUTPUT_STREAM_MONITOR, hostname, monitor));
        count = 0L;

    }
}