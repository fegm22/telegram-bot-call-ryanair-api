package org.ryanairbot.services;

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
import org.json.JSONObject;
import org.ryanairbot.domain.Airport;
import org.ryanairbot.domain.Leg;
import org.ryanairbot.domain.Route;
import org.ryanairbot.domain.RouteDetail;
import org.ryanairbot.helpers.AirportService;
import org.ryanairbot.helpers.RouteService;
import org.ryanairbot.helpers.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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


public class RyanairService {
    private static final String LOGTAG = "RYANAIR";
    private static final String BASEURL = "http://localhost:8092/commands"; ///< Base url for REST

    /**
     * This method will return the response base on the questions ask in telegram
     *
     * @param query Text from Telegram
     * @return Return the answer
     */
    public String processMessage(String query) {

        String result;

        RouteService routeService = new RouteService();
        List<Route> routes = routeService.getRoutes();

        Map<Integer, String> cities = getCitiesQuery(query, routes);

        //result = getRequestCommand(query);

        if (cities.size() == 2) {
            String departure = cities.get(0);
            String arrival = cities.get(1);

            String instruction = getInstruction(query);

            result = getResultMessage(routes, departure, arrival, instruction);

        } else {
            result = helpMessage();
        }

        return result;
    }

    private String getResultMessage(List<Route> routes, String departure, String arrival, String instruction) {
        String result = "";
        if (instruction.isEmpty()) {
            result = "I didn't understand your question. But here are the direct flights of the week \n\n";

            result = result + getFlightsDirect(departure, arrival,
                    LocalDateTime.now(),
                    LocalDateTime.now().plusWeeks(1));

        } else {
            if (instruction.toUpperCase().equals("CONNECTIONS")) {
                result = getConnectionsResponse(departure, arrival, routes);
            }
            if (instruction.toUpperCase().equals("FLIGHTS")) {
                result = getFlightsDirect(departure, arrival,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusWeeks(1));
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


    private Map<Integer, String> getCitiesQuery(String query, List<Route> routes) {
        RouteService routeService = new RouteService();
        Map<String, Set<String>> mapRoute = routeService.createRouteMap(routes);

        String[] items = query.split(" ");
        List<String> queryWords = Arrays.asList(items);

        int contador = 0;
        Map<Integer, String> cities = new HashMap<>();
        for (String word : queryWords) {
            if (mapRoute.containsKey(word.toUpperCase())) {
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

    private String getConnectionsResponse(String departure, String arrival, List<Route> routes) {
        String result;

        RouteService routeService = new RouteService();
        AirportService airportService = new AirportService();
        List<String> connections = routeService.getIataConnections(routes, departure, arrival);
        List<Airport> airportList = airportService.getAirports();
        Map<String, String> airportMap = airportService.createAirportMap(airportList);

        result = "This are all the connections between " + departure + " and " + arrival + "\n\n";

        for (String citiConnect : connections) {

            if(airportMap.containsKey(citiConnect)){
                result = result + airportMap.get(citiConnect).toString().replace("_", " ") + "\n";
            }else{
                result = result + citiConnect + "\n";
            }
        }
        return result;
    }

    private String getFlightsDirect(String departure, String arrival, LocalDateTime localDepartureDateTime,
                                    LocalDateTime localArrivalDateTime) {
        ScheduleService scheduleService = new ScheduleService();
        Map<Map<String, String>, List<RouteDetail>> mapDirectFlight =
                scheduleService.getRoutesScheduleDetail(departure, arrival, localDepartureDateTime, localArrivalDateTime);
        List<Leg> directFlights = new ArrayList<>();
        Map<String, String> keyFlight = new HashMap<>();
        keyFlight.put(departure, arrival);

        if (!mapDirectFlight.isEmpty())
            directFlights = mapDirectFlight.get(keyFlight).get(0).getMapRoute().get(keyFlight);

        String result = "The flights from " + departure + " and " + arrival + " for this week are :\n\n";
        result = result + "Departure                      Arrival \n";
        for (Leg leg : directFlights) {
            result = result + leg.getDepartureDateTime() + "        " + leg.getArrivalDateTime() + "\n";
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
