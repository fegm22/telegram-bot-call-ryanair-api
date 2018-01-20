package org.ryanairbot.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Day implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer day;
    private List<Flight> flights;

}
