package org.ryanairbot.helpers;

import org.ryanairbot.domain.Airport;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AirportService {

    /**
     * This method will return the routes provided by the Ryanair API.
     *
     * @return List<Route> with stop 1 and the list of the interconnected flights
     */
    public List<Airport> getAirports() {

        final String uri = "https://api.ryanair.com/core/3/airports/";
        ParameterizedTypeReference<List<Airport>> responseType = new ParameterizedTypeReference<List<Airport>>() {
        };

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<Airport>> routes = restTemplate.exchange(uri,
                HttpMethod.GET, null,
                responseType);

        List<Airport> listRoutes = routes.getBody();

        return listRoutes;

    }

    /**
     * This method will return a map of all airports and his connections
     *
     * @param airportList
     * @return a map of every Airport and his connections in a list.
     */
    public Map<String, String> createAirportMap(List<Airport> airportList) {
        Map<String, String> mapAirport = new HashMap<>();

        for (Airport airport : airportList) {
            String iataCode = airport.getIataCode();
            String citiCode = airport.getCityCode();

            mapAirport.put(iataCode, citiCode);
        }

        return mapAirport;
    }

}
