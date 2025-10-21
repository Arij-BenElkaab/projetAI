package tn.esprit.ia.covoiturage.dto;

public class PredictionRequestDTO {
    private double tripDistance;
    private double pickupDistanceKm;
    private double price;
    private double tripSeconds;

    public double getTripDistance() {
        return tripDistance;
    }

    public void setTripDistance(double tripDistance) {
        this.tripDistance = tripDistance;
    }

    public double getPickupDistanceKm() {
        return pickupDistanceKm;
    }

    public void setPickupDistanceKm(double pickupDistanceKm) {
        this.pickupDistanceKm = pickupDistanceKm;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getTripSeconds() {
        return tripSeconds;
    }

    public void setTripSeconds(double tripSeconds) {
        this.tripSeconds = tripSeconds;
    }
}
