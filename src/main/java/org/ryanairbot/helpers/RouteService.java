package org.ryanairbot.helpers;

import org.ryanairbot.domain.Route;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class RouteService {

    /**
     * This method will return the routes provided by the Ryanair API.
     * @return List<Route> with stop 1 and the list of the interconnected flights
     */
    public List<Route> getRoutes() {

        final String uri = "https://api.ryanair.com/core/3/routes/";
        ParameterizedTypeReference<List<Route>> responseType = new ParameterizedTypeReference<List<Route>>() {
        };

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<Route>> routes = restTemplate.exchange(uri,
                HttpMethod.GET, null,
                responseType);

        List<Route> listRoutes = routes.getBody();

        return listRoutes;

    }

    /**
     * This method will return a list of all airports who connect departure with arrival
     * It use the list of routes returned by the Ryanair API
     * @param listRoutes
     * @param departure IATA Code for Departure Airport
     * @param arrival IATA Code for Arrival Airport
     * @return list of IATA code connections
     */
    public List<String> getIataConnections(List<Route> listRoutes, String departure, String arrival) {
        Map<String, Set<String>> mapRoute = createRouteMap(listRoutes);
        List<String> airportsConnection = new ArrayList<>();
        Set<String> arrivalFromOrigin = new HashSet<>();

        for (String airportFrom : mapRoute.keySet()) {
            if (airportFrom.equals(departure)) {
                arrivalFromOrigin = mapRoute.get(airportFrom);
                break;
            }
        }

        for (String connection : arrivalFromOrigin) {
            Set<String> destiny = mapRoute.get(connection);
            if (!destiny.equals(arrival) && destiny.contains(arrival)) {
                airportsConnection.add(connection);
            }
        }

        return airportsConnection;
    }

    /**
     * This method will return a map of all airports and his connections
     * @param listRoutes
     * @return a map of every Airport and his connections in a list.
     */
    public Map<String, Set<String>> createRouteMap(List<Route> listRoutes) {
        Map<String, Set<String>> mapRoute = new HashMap<>();

        for (Route route : listRoutes) {
            String airportFrom = route.getAirportFrom();
            String airportTo = route.getAirportTo();

            if (mapRoute.containsKey(airportFrom)) {
                Set<String> airportsTo = mapRoute.get(airportFrom);
                airportsTo.add(airportTo);
                mapRoute.put(airportFrom, airportsTo);
            } else {
                Set<String> setTo = new HashSet<>();
                setTo.add(airportTo);
                mapRoute.put(airportFrom, setTo);
            }

        }

        return mapRoute;
    }

}
