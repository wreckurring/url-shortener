package com.wreckurring.urlshortener.service;

import com.wreckurring.urlshortener.model.ClickEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsProducer {
    @Autowired
    private KafkaTemplate<String, ClickEvent> kafkaTemplate;

    public void sendClickEvent(ClickEvent event) {
        kafkaTemplate.send("url-clicks", event);
    }
}