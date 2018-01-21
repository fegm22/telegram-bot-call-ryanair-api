package org.ryanairbot.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.ryanairbot.model.Airport;
import org.ryanairbot.model.Route;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteDto implements Serializable {

    private final Route route;
    private final Boolean newRoute;
    private final Boolean seasonalRoute;

    public static RouteDto of(Airport airportFrom, Airport airportTo) {
        return new RouteDto(airportFrom, airportTo, false, false);
    }

    @JsonCreator
    public RouteDto(@JsonProperty("airportFrom") Airport airportFrom,
                    @JsonProperty("airportTo") Airport airportTo,
                    @JsonProperty("newRoute") Boolean newRoute,
                    @JsonProperty("seasonalRoute") Boolean seasonalRoute) {

        this.route = new Route(airportFrom, airportTo);

        this.newRoute = newRoute;
        this.seasonalRoute = seasonalRoute;
    }

    public Airport getAirportFrom() {
        return route.getFrom();
    }

    public Airport getAirportTo() {
        return route.getTo();
    }
}
