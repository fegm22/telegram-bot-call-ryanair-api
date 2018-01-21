package org.ryanairbot.service;

import org.ryanairbot.dto.FlightDto;
import org.ryanairbot.model.Airport;
import org.ryanairbot.model.Flight;
import org.ryanairbot.model.Route;
import org.ryanairbot.utils.Constants;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

public class SearchFlightsService {


    private SearchRouteService searchRouteService = new SearchRouteService();
    private SchedulesService scheduleService = new SchedulesService();

    @Cacheable(Constants.CACHE_FLIGHTS)
    public List<FlightDto> findFlights(
            Airport departure,
            Airport arrival,
            LocalDateTime departureDateTime,
            LocalDateTime arrivalDateTime,
            int maxStops,
            int minTransferMinutes,
            int maxTransferMinutes) {

        if (departure.equals(arrival)) return emptyList();
        if (departureDateTime.compareTo(arrivalDateTime) >= 0) return emptyList();

        List<YearMonth> months = getMonthsBetween(departureDateTime, arrivalDateTime);
        List<List<Route>> routingList = searchRouteService.findRoutesBetween(departure, arrival, maxStops);

        BiFunction<List<List<Flight>>, List<Flight>, List<List<Flight>>> lookupLegs = getLookupNextLegs(arrivalDateTime, minTransferMinutes, maxTransferMinutes);

        return routingList.stream()
                .flatMap(routing -> getFlightStream(departureDateTime, arrivalDateTime, months, lookupLegs, routing))
                .collect(Collectors.toList());
    }

    private Stream<? extends FlightDto> getFlightStream(LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, List<YearMonth> months, BiFunction<List<List<Flight>>, List<Flight>, List<List<Flight>>> lookupLegs, List<Route> routing) {
        List<List<Flight>> flightsPerLeg = getFlightsPerLeg(departureDateTime, arrivalDateTime, months, routing);
        List<List<Flight>> legsList = initializeLegList(flightsPerLeg);

        if (flightsPerLeg.size() > 1) {
            for (List<Flight> nextLegFlights : flightsPerLeg.subList(1, flightsPerLeg.size())) {
                legsList = lookupLegs.apply(legsList, nextLegFlights);
            }
        }

        return legsList.stream().map(legs -> new FlightDto(legs.size() - 1, legs));
    }

    public List<List<Flight>> getFlightsPerLeg(LocalDateTime departureDateTime, LocalDateTime arrivalDateTime, List<YearMonth> months, List<Route> routing) {
        return routing.stream()
                .map(route -> getFlights(route.getFrom(), route.getTo(), months, departureDateTime, arrivalDateTime))
                .collect(Collectors.toList());
    }

    public List<Flight> getFlights(Airport departure, Airport arrival, List<YearMonth> months, LocalDateTime from, LocalDateTime to) {
        return months.parallelStream()
                .map(month -> scheduleService.getFlightsSchedules(departure, arrival, month))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(t -> t.getFlights().stream())
                .filter(f -> !f.getDepartureTime().isBefore(from) && !f.getArrivalTime().isAfter(to))
                .collect(Collectors.toList());
    }

    public List<List<Flight>> initializeLegList(List<List<Flight>> flightsPerLeg) {
        return flightsPerLeg.get(0).stream()
                .map(Collections::singletonList)
                .collect(Collectors.toList());
    }

    public BiFunction<List<List<Flight>>, List<Flight>, List<List<Flight>>> getLookupNextLegs(
            LocalDateTime maxArrivalDateTime,
            int minTransferMinutes,
            int maxTransferMinutes
    ) {
        return (List<List<Flight>> currentLegsList, List<Flight> nextLegFlights) -> currentLegsList.stream()
                .flatMap(currentLegs -> getListFlightStream(maxArrivalDateTime, minTransferMinutes, maxTransferMinutes, nextLegFlights, currentLegs))
                .collect(Collectors.toList());
    }

    private Stream<? extends List<Flight>> getListFlightStream(LocalDateTime maxArrivalDateTime, int minTransferMinutes, int maxTransferMinutes, List<Flight> nextLegFlights, List<Flight> currentLegs) {
        Flight lastLeg = currentLegs.get(currentLegs.size() - 1);
        LocalDateTime earliestDeparture = lastLeg.getArrivalTime().plusMinutes(minTransferMinutes);
        LocalDateTime latestDeparture = lastLeg.getArrivalTime().plusMinutes(maxTransferMinutes);

        return nextLegFlights.stream()
                .filter(flight -> checkFlightTimes(maxArrivalDateTime, earliestDeparture, latestDeparture, flight))
                .map(flight -> getLegs(currentLegs, flight));
    }

    private boolean checkFlightTimes(LocalDateTime maxArrivalDateTime, LocalDateTime earliestDeparture, LocalDateTime latestDeparture, Flight flight) {
        return !flight.getDepartureTime().isBefore(earliestDeparture)
                && !flight.getDepartureTime().isAfter(latestDeparture)
                && !flight.getArrivalTime().isAfter(maxArrivalDateTime);
    }

    private List<Flight> getLegs(List<Flight> currentLegs, Flight flight) {
        List<Flight> newLegs = new LinkedList<>(currentLegs);
        newLegs.add(flight);

        return newLegs;
    }

    public static List<YearMonth> getMonthsBetween(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            List<YearMonth> months = getMonthsBetween(end, start);
            Collections.reverse(months);
            return months;
        } else {
            YearMonth from = YearMonth.from(start);
            YearMonth to = YearMonth.from(end);
            List<YearMonth> months = new LinkedList<>();
            months.add(from);
            if (to.isAfter(from)) {
                YearMonth m = from;
                do {
                    m = m.plusMonths(1);
                    months.add(m);
                }
                while (m.isBefore(to));
            }
            return months;
        }
    }


}
