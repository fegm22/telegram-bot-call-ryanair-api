package org.ryanairbot.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.ryanairbot.model.Flight;

import javax.validation.ValidationException;
import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightDto implements Serializable {

    private final int stops;
    private final Collection<Flight> legs;

    @JsonCreator
    public FlightDto(
        @JsonProperty("stops") int stops,
        @JsonProperty("legs") Collection<Flight> legs) {
            if(legs==null){
                throw new ValidationException("Flights collection MAY NOT null");
            }
            this.legs = legs.stream().collect(Collectors.toList());
            this.stops = stops;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FlightDto{");
        sb.append("stops=").append(stops);
        sb.append(", legs=").append(legs);
        sb.append('}');
        return sb.toString();
    }
}
