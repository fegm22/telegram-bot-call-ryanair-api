package org.ryanairbot.service;

import org.ryanairbot.dto.CitiesDto;
import org.ryanairbot.integration.CitiesApiClient;
import org.ryanairbot.utils.Constants;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CitiesServices {

    private CitiesApiClient citiesApiClient = new CitiesApiClient(new RestTemplate(),"https://api.ryanair.com/core/3/airports/");

    @Cacheable(Constants.CACHE_AIRPORTS)
    public Map<String, String> getAllAvailableAirports() {
        Map<String, String> map = new HashMap<>();
        List<CitiesDto> airports = citiesApiClient.getAirports();

        airports.stream().forEach(CitiesDto -> addCity(map, CitiesDto));

        return map;
    }

    private void addCity(Map<String, String> map, CitiesDto city) {
        String citiCode = city.getCity().getCitiCodeCode();
        String iataCode = city.getCity().getIataCode();

        map.putIfAbsent(iataCode, citiCode);
    }
}
