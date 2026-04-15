package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class WikidataMountainGetDTO {

    private String wikidataEntityId;
    private String mountainName;
    private String imageUrl;
    private double latitude;
    private double longitude;

    public String getWikidataEntityId() {
        return wikidataEntityId;
    }

    public void setWikidataEntityId(String wikidataEntityId) {
        this.wikidataEntityId = wikidataEntityId;
    }

    public String getMountainName() {
        return mountainName;
    }

    public void setMountainName(String mountainName) {
        this.mountainName = mountainName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
