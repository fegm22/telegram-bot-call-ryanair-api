package org.ryanairbot.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Airport implements Serializable{

    private static final long serialVersionUID = 1L;

    private String iataCode;
    private String name;
    private String cityCode;

}
