package org.ryanairbot.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Route implements Serializable {

    private static final long serialVersionUID = 1L;

    private String airportFrom;
    private String airportTo;

}
