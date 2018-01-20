package org.ryanairbot.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Flight implements Serializable {

    private static final long serialVersionUID = 1L;

    private String departureTime;
    private String arrivalTime;

}
