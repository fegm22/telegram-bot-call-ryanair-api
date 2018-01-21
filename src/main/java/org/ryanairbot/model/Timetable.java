package org.ryanairbot.model;

import lombok.Data;

import java.io.Serializable;
import java.time.YearMonth;
import java.util.Collection;

@Data
public class Timetable implements Serializable {

    private final Route route;
    private final YearMonth yearMonth;
    private Collection<Flight> flights;

    public Timetable(Route route, YearMonth yearMonth, Collection<Flight> flights) {
        this.route = route;
        this.yearMonth = yearMonth;
        this.flights = flights;
    }

}
