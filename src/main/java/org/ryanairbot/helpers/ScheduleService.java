package org.ryanairbot.helpers;

import lombok.extern.slf4j.Slf4j;
import org.ryanairbot.domain.Day;
import org.ryanairbot.domain.Flight;
import org.ryanairbot.domain.Leg;
import org.ryanairbot.domain.RouteDetail;
import org.ryanairbot.domain.Schedule;
import org.ryanairbot.utils.Utils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class ScheduleService {

    /**
     * This method will return the Schedules for a specific route giving a year and a month
     * Use the services provided by the Ryanair API.
     * @param departure IATA Code for Departure Airport
     * @param arrival IATA Code for Arrival Airport
     * @param year number of the year to request time flights
     * @param month number of the month to request time flights
     * @return Schedule: time flights for a giving year/month
     */
    public Schedule getSchedules(String departure, String arrival, int year, int month) {

        final String uri = "https://api.ryanair.com/timetable/3/schedules/{departure}/{arrival}/years/{year}/months/{month}";
        final ParameterizedTypeReference<Schedule> responseType = new ParameterizedTypeReference<Schedule>() {
        };

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Schedule> responseSchedules = restTemplate.exchange(uri,
                HttpMethod.GET, null,
                responseType,
                departure,
                arrival,
                year,
                month);

        Schedule schedules = responseSchedules.getBody();

        return schedules;
    }

    /**
     * This method will return the detail information of the schedules routes
     * It will check every flight between the range dates
     * @param departure IATA Code for Departure Airport
     * @param arrival IATA Code for Arrival Airport
     * @param localDepartureDateTime departure date time in ISO Format
     * @param localArrivalDateTime arrival date time in ISO Format
     * @return Map of Key Routes <departure, arrival> and a list of every time flight filter by the range dates
     */
    public Map<Map<String, String>, List<RouteDetail>> getRoutesScheduleDetail(String departure, String arrival,
                                                                          LocalDateTime localDepartureDateTime, LocalDateTime localArrivalDateTime) {

        Map<Map<String, String>, List<RouteDetail>> scheduleMap = new HashMap<>();

        for (LocalDateTime date = localDepartureDateTime; date.isBefore(localArrivalDateTime); date = date.plusMonths(1)) {

            int year = date.getYear();
            int month = date.getMonthValue();

            try {
                Schedule schedule = this.getSchedules(departure, arrival, year, month);

                if (!schedule.getDays().isEmpty()) {

                    for (Day day : schedule.getDays()) {

                        for (Flight flight : day.getFlights()) {

                            LocalDateTime flighTimeDeparture = Utils.covertLocalDateTime(year, month, day.getDay(), flight.getDepartureTime());
                            LocalDateTime flighTimeArrival = Utils.covertLocalDateTime(year, month, day.getDay(), flight.getArrivalTime());

                            if (flighTimeDeparture.isAfter(localDepartureDateTime) && flighTimeArrival.isBefore(localArrivalDateTime)) {

                                Leg leg = new Leg(departure, arrival, flighTimeDeparture, flighTimeArrival);
                                Map<String, String> keyRoute = new HashMap<>();
                                keyRoute.put(departure, arrival);

                                if (scheduleMap.containsKey(keyRoute)) {

                                    List<RouteDetail> listRouteDetail = scheduleMap.get(keyRoute);
                                    RouteDetail route = listRouteDetail.get(0);
                                    Map<Map<String, String>, List<Leg>> routeMap = route.getMapRoute();
                                    List<Leg> legExist = routeMap.get(keyRoute);

                                    legExist.add(leg);
                                    routeMap.put(keyRoute, legExist);
                                    route.setMapRoute(routeMap);

                                    if (route.getMinTimeFlightDeparture().isAfter(flighTimeDeparture))
                                        route.setMinTimeFlightDeparture(flighTimeDeparture);
                                    if (route.getMinTimeFlightArrival().isAfter(flighTimeArrival))
                                        route.setMinTimeFlightArrival(flighTimeArrival);
                                    if (route.getMaxTimeFlightDeparture().isBefore(flighTimeDeparture))
                                        route.setMaxTimeFlightDeparture(flighTimeDeparture);
                                    if (route.getMaxTimeFlightArrival().isBefore(flighTimeArrival))
                                        route.setMaxTimeFlightArrival(flighTimeArrival);

                                    scheduleMap.put(keyRoute, listRouteDetail);

                                } else {

                                    List<RouteDetail> listNewRouteDetail = new ArrayList<>();
                                    RouteDetail newRoute = new RouteDetail();
                                    Map<Map<String, String>, List<Leg>> newRouteMap = new HashMap<>();
                                    List<Leg> listNewFlightsDateTime = new ArrayList<>();

                                    listNewFlightsDateTime.add(leg);
                                    newRouteMap.put(keyRoute, listNewFlightsDateTime);
                                    newRoute.setMapRoute(newRouteMap);

                                    newRoute.setMinTimeFlightDeparture(flighTimeDeparture);
                                    newRoute.setMinTimeFlightArrival(flighTimeArrival);
                                    newRoute.setMaxTimeFlightDeparture(flighTimeDeparture);
                                    newRoute.setMaxTimeFlightArrival(flighTimeArrival);

                                    listNewRouteDetail.add(newRoute);

                                    scheduleMap.put(keyRoute, listNewRouteDetail);
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                log.debug("No data for " + departure + " to " + arrival + " - year: " + year + " month: " + month);
            }
        }

        return scheduleMap;
    }
}
