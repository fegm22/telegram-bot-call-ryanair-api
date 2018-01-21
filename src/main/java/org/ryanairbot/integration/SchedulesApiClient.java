package org.ryanairbot.integration;

import org.ryanairbot.dto.SchedulesDto;
import org.ryanairbot.model.Airport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.YearMonth;
import java.util.Optional;

public class SchedulesApiClient extends BaseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulesApiClient.class);

    public SchedulesApiClient(RestTemplate template, String baseUrl) {
        this.template = template;
        this.baseUrl = baseUrl;
    }

    public Optional<SchedulesDto> getSchedules(Airport departure, Airport arrival, YearMonth yearMonth) {
        try {
            ParameterizedTypeReference<SchedulesDto> type = new ParameterizedTypeReference<SchedulesDto>() {};
            ResponseEntity<SchedulesDto> instance = template.exchange(baseUrl, HttpMethod.GET, null, type, departure.getIataCode(), arrival.getIataCode(), yearMonth.getYear(), yearMonth.getMonthValue());

            LOGGER.info("GET schedules from Schedules API (" + departure + "->" + arrival + "," + yearMonth + ")");

            if (instance.getStatusCode() == HttpStatus.OK) {
                return Optional.of(instance.getBody());
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            LOGGER.error("Unable to get schedules from Schedules API for (" + departure + "->" + arrival + "," + yearMonth + ")");
            return Optional.empty();
        }
    }
}
