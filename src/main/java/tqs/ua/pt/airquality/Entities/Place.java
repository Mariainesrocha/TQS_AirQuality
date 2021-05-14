package tqs.ua.pt.airquality.Entities;

import java.util.*;

public class Place {

    private String name;

    private Double latitude;

    private Double longitude;

    private Date updateDate;

    private Map<String, Double> airElements = new HashMap<>();

    private List<String> airQualityInfo = new ArrayList<>();

    private Map<String, String> pollen_risk = new HashMap<>();

    private Map<String, Integer> pollen_count = new HashMap<>();

    public Place(String name, double lat,double lng) {
        this.name = name;
        this.latitude = lat;
        this.longitude = lng;
    }

    public Place() {}

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Map<String, Double> getAirElements() {
        return airElements;
    }

    public List<String> getAirQualityInfo() {
        return airQualityInfo;
    }

    public Map<String, String> getPollen_risk() {
        return pollen_risk;
    }

    public Map<String, Integer> getPollen_count() {
        return pollen_count;
    }

    public void setAirElements(Map<String, Double> airElements) {
        this.airElements = airElements;
    }

    public void setAirQualityInfo(List<String> airQualityInfo) {
        this.airQualityInfo = airQualityInfo;
    }

    public void setPollen_count(Map<String, Integer> pollen_count) {
        this.pollen_count = pollen_count;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getName() {
        return name;
    }
}