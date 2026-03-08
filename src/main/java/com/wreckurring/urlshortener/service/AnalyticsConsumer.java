package com.wreckurring.urlshortener.service;

import com.wreckurring.urlshortener.model.ClickEvent;
import com.wreckurring.urlshortener.model.Url;
import com.wreckurring.urlshortener.model.UrlClick;
import com.wreckurring.urlshortener.repository.UrlClickRepository;
import com.wreckurring.urlshortener.repository.UrlRepository;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class AnalyticsConsumer {
    @Autowired private UrlRepository urlRepository;
    @Autowired private UrlClickRepository urlClickRepository;

    @KafkaListener(topics = "url-clicks", groupId = "analytics-group")
    @Transactional
    public void consume(ClickEvent event) {
        Optional<Url> urlOptional = urlRepository.findByShortCode(event.getShortCode());
        if (urlOptional.isPresent()) {
            Url url = urlOptional.get();
            url.incrementClickCount();
            urlRepository.save(url);

            UserAgent ua = UserAgent.parseUserAgentString(event.getUserAgent());
            UrlClick click = new UrlClick(url, event.getIpAddress(), event.getUserAgent(), event.getReferer());
            click.setDeviceType(ua.getOperatingSystem().getDeviceType().getName());
            click.setBrowser(ua.getBrowser().getName());
            click.setCountry(getCountryFromIp(event.getIpAddress()));

            urlClickRepository.save(click);
        }
    }

    private String getCountryFromIp(String ip) {
        return (ip == null || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")) ? "Local" : "Unknown";
    }
}