package org.ryanairbot.bot;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.json.JSONObject;
import org.ryanairbot.dto.FlightDto;
import org.ryanairbot.model.Airport;
import org.ryanairbot.model.Flight;
import org.ryanairbot.model.Route;
import org.ryanairbot.service.impl.CitiesServices;
import org.ryanairbot.service.impl.RoutesService;
import org.ryanairbot.service.impl.SearchFlightsService;
import org.ryanairbot.service.impl.SearchRouteService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.logging.BotLogger;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class RyanairService {
    private static final String LOGTAG = "RYANAIR";
    private static final String BASEURL = "http://localhost:8092/commands"; ///< Base url for REST


    private SearchRouteService searchRouteService = new SearchRouteService();
    private SearchFlightsService searchFlightsService = new SearchFlightsService();
    private RoutesService routesService = new RoutesService();
    private CitiesServices citiesServices = new CitiesServices();

    /**
     * This method will return the response base on the questions ask in telegram
     *
     * @param query Text from Telegram
     * @return Return the answer
     */
    public String processMessage(String query) {

        String result;

        DirectedGraph<Airport, DefaultEdge> routes = routesService.getAllAvailableRoutes();

        Map<String, String> citiesMap = citiesServices.getAllAvailableAirports();

        Map<Integer, String> cities = getCitiesQuery(query, routes);

        //result = getRequestCommand(query);

        if (cities.size() == 2) {
            Airport departure = new Airport(cities.get(0));
            Airport arrival = new Airport(cities.get(1));

            String instruction = getInstruction(query);

            result = getResultMessage(departure, arrival, instruction, citiesMap);

        } else {
            result = helpMessage();
        }

        return result;
    }

    private String getResultMessage(Airport departure, Airport arrival, String instruction, Map<String, String> citiesGraph) {
        String result = "";
        if (instruction.isEmpty()) {
            result = "I didn't understand your question. But here are the direct flights of the week \n\n";

            result = result + getFlightsDirect(departure, arrival,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusWeeks(1),
                    citiesGraph);

        } else {
            if (instruction.toUpperCase().equals("CONNECTIONS")) {
                result = getConnectionsResponse(departure, arrival, citiesGraph);
            }
            if (instruction.toUpperCase().equals("FLIGHTS")) {
                result = getFlightsDirect(departure, arrival,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusWeeks(1),
                        citiesGraph);
            }
        }
        return result;
    }

    public String getRequestCommand(String query) {

        String completeURL;
        String result = null;

        try {
            completeURL = BASEURL + URLEncoder.encode("", "UTF-8");

            CloseableHttpClient client = HttpClientBuilder.create().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
            HttpPost request = new HttpPost(completeURL);

            List<NameValuePair> params = new ArrayList<>(2);
            params.add(new BasicNameValuePair("message", query));
            request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            CloseableHttpResponse response = client.execute(request);
            HttpEntity ht = response.getEntity();

            BufferedHttpEntity buf = new BufferedHttpEntity(ht);
            String responseString = EntityUtils.toString(buf, "UTF-8");

            JSONObject jsonObject = new JSONObject(responseString);

            String message = jsonObject.getString("header") + "\n\n" + jsonObject.getString("message");

            result = message;

        } catch (IOException e) {
            BotLogger.error(LOGTAG, e);
        }

        return result;
    }


    private Map<Integer, String> getCitiesQuery(String query, DirectedGraph<Airport, DefaultEdge> routes) {
        String[] items = query.split(" ");
        List<String> queryWords = Arrays.asList(items);

        int contador = 0;
        Map<Integer, String> cities = new HashMap<>();
        for (String word : queryWords) {
            Airport airport = new Airport(word);
            if (routes.containsVertex(airport)) {
                cities.put(contador++, word.toUpperCase());
            }
        }
        return cities;
    }

    private String getInstruction(String query) {
        Set<String> setInstructions = new HashSet<>();
        setInstructions.add("CONNECTIONS");
        setInstructions.add("FLIGHTS");

        String[] items = query.split(" ");
        List<String> queryWords = Arrays.asList(items);

        String instruction = "";
        for (String word : queryWords) {
            if (setInstructions.contains(word.toUpperCase())) {
                instruction = word.toUpperCase();
            }
        }
        return instruction;
    }

    private String getConnectionsResponse(Airport departure, Airport arrival, Map<String, String> cities) {
        String result;

        List<List<Route>> connections = searchRouteService.findRoutesBetween(departure, arrival, 0);

        result = "This are all the connections between " + cities.get(departure.getIataCode()) + " and " + cities.get(arrival.getIataCode()) + "\n\n";

        List<Route> routingList = connections.get(0);

        if (routingList.size() > 1) {
            for (Route citiConnect : routingList) {
                result = result + cities.get(citiConnect.getFrom().getIataCode()).replace("_", " ") + "\n";
            }
        } else {
            result = "There is no connection!!! You can travel directly from " + cities.get(departure.getIataCode()) + " to " + cities.get(arrival.getIataCode()) + "\n\n";;
        }

        return result;
    }

    private String getFlightsDirect(Airport departure, Airport arrival,
                                    LocalDateTime localDepartureDateTime,
                                    LocalDateTime localArrivalDateTime,
                                    Map<String, String> cities) {


        List<FlightDto> directFlights = searchFlightsService.findFlights(departure, arrival, localDepartureDateTime, localArrivalDateTime, 0, 0, 0);

        String result = "";
        if (!directFlights.isEmpty()) {
            result = "The flights from " + cities.get(departure.getIataCode()) + " and " + cities.get(arrival.getIataCode()) + " for this week are :\n\n";
            result = result + "Number      Departure                      Arrival \n";

            for (FlightDto leg : directFlights) {
                result = result +
                        leg.getLegs().stream().map(Flight::getNumber).collect(Collectors.toList()).get(0).toString() + "            " +
                        leg.getLegs().stream().map(Flight::getDepartureTime).collect(Collectors.toList()).get(0).toString() + "        " +
                        leg.getLegs().stream().map(Flight::getArrivalTime).collect(Collectors.toList()).get(0).toString() + "\n";
            }
        }

        return result;
    }

    private String helpMessage() {
        String message = "Sorry but I couldn't understand your question. \n\n";

        message = message + "Try something like...\n\n";
        message = message + "- Give me the CONNECTIONS between MAD and DUB\n";
        message = message + "- Give me FLIGHTS between MAD and WRO\n\n";
        return message;
    }

    private String getGETRequest(String query) {
        final String uri = BASEURL;
        ParameterizedTypeReference<String> responseType = new ParameterizedTypeReference<String>() {
        };


        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> routes = restTemplate.exchange(uri,
                HttpMethod.GET, null,
                responseType);

        String responseString = routes.getBody();
        JSONObject jsonObject = new JSONObject(responseString);

        String message = jsonObject.getString("header") + "\n\n" + jsonObject.getString("message");

        return message;

    }

}
