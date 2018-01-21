package org.ryanairbot.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.validation.ValidationException;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class City implements Serializable {

    private final String iataCode;
    private final String cityCode;

    public City(String iataCode) {
        this.iataCode = iataCode;
        this.cityCode = "";
    }

    public City(String iataCode, String cityCode) {
        this.iataCode = iataCode;
        this.cityCode = cityCode;
    }

    @JsonValue
    public String getIataCode() {
        return iataCode;
    }

    @JsonValue
    public String getCitiCodeCode() {
        return cityCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        City city = (City) o;
        return Objects.equals(iataCode, city.iataCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iataCode);
    }

    @Override
    public String toString() {
        return iataCode;
    }

    private static final ConcurrentHashMap<String,City> AIRPORTS = new ConcurrentHashMap<>();

    @JsonCreator
    public static City of(@JsonProperty("iataCode") String iataCode, @JsonProperty("cityCode") String cityCode) {
        if(iataCode==null || iataCode.isEmpty() || iataCode.length()>3){
            throw new ValidationException("Invalid IATA code format: "+iataCode);
        }
        return AIRPORTS.computeIfAbsent(iataCode, code -> new City(code, cityCode));
    }

}
