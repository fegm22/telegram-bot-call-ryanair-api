package org.ryanairbot.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Interconnection implements Serializable {

    private final static Long serialVersionUID = 1L;

    private Integer stops;
    private List<Leg> legs;

    public Interconnection(Integer stops, List<Leg> legs){
        this.stops = stops;
        this.legs = legs;
    }
}
