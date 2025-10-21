package tn.esprit.ia.covoiturage.dto;

import java.time.LocalDateTime;

public class TripDTO {
    private String tripId;
    private String driverId;
    private String passengerId;
    private double pickupLatitude;
    private double pickupLongitude;
    private double dropoffLatitude;
    private double dropoffLongitude;
    private LocalDateTime pickupTime;
    private LocalDateTime dropoffTime;
    private double tripDistance;
    private double price;
    private boolean prefMusic;
    private boolean prefSmoke;
    private boolean prefAnimals;

    public TripDTO(String tripId, String driverId, String passengerId, double pickupLatitude, double pickupLongitude, double dropoffLatitude, double dropoffLongitude, LocalDateTime pickupTime, LocalDateTime dropoffTime, double tripDistance, double price, boolean prefMusic, boolean prefSmoke, boolean prefAnimals) {
        this.tripId = tripId;
        this.driverId = driverId;
        this.passengerId = passengerId;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.dropoffLatitude = dropoffLatitude;
        this.dropoffLongitude = dropoffLongitude;
        this.pickupTime = pickupTime;
        this.dropoffTime = dropoffTime;
        this.tripDistance = tripDistance;
        this.price = price;
        this.prefMusic = prefMusic;
        this.prefSmoke = prefSmoke;
        this.prefAnimals = prefAnimals;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public double getPickupLatitude() {
        return pickupLatitude;
    }

    public void setPickupLatitude(double pickupLatitude) {
        this.pickupLatitude = pickupLatitude;
    }

    public double getPickupLongitude() {
        return pickupLongitude;
    }

    public void setPickupLongitude(double pickupLongitude) {
        this.pickupLongitude = pickupLongitude;
    }

    public double getDropoffLatitude() {
        return dropoffLatitude;
    }

    public void setDropoffLatitude(double dropoffLatitude) {
        this.dropoffLatitude = dropoffLatitude;
    }

    public double getDropoffLongitude() {
        return dropoffLongitude;
    }

    public void setDropoffLongitude(double dropoffLongitude) {
        this.dropoffLongitude = dropoffLongitude;
    }

    public LocalDateTime getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(LocalDateTime pickupTime) {
        this.pickupTime = pickupTime;
    }

    public LocalDateTime getDropoffTime() {
        return dropoffTime;
    }

    public void setDropoffTime(LocalDateTime dropoffTime) {
        this.dropoffTime = dropoffTime;
    }

    public double getTripDistance() {
        return tripDistance;
    }

    public void setTripDistance(double tripDistance) {
        this.tripDistance = tripDistance;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isPrefMusic() {
        return prefMusic;
    }

    public void setPrefMusic(boolean prefMusic) {
        this.prefMusic = prefMusic;
    }

    public boolean isPrefSmoke() {
        return prefSmoke;
    }

    public void setPrefSmoke(boolean prefSmoke) {
        this.prefSmoke = prefSmoke;
    }

    public boolean isPrefAnimals() {
        return prefAnimals;
    }

    public void setPrefAnimals(boolean prefAnimals) {
        this.prefAnimals = prefAnimals;
    }
}

