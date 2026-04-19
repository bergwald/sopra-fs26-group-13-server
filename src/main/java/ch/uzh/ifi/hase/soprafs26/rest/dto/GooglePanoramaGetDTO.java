package ch.uzh.ifi.hase.soprafs26.rest.dto;

// Temporary response contract for the `/google/panorama` demo route.
public class GooglePanoramaGetDTO {

    private String provider;
    private String panoId;
    private double latitude;
    private double longitude;

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getPanoId() {
        return panoId;
    }

    public void setPanoId(String panoId) {
        this.panoId = panoId;
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
