package org.ryanairbot.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class RouteDetail {

    private Map<Map<String, String>, List<Leg>> mapRoute;
    private LocalDateTime minTimeFlightDeparture;
    private LocalDateTime minTimeFlightArrival;
    private LocalDateTime maxTimeFlightDeparture;
    private LocalDateTime maxTimeFlightArrival;

}
