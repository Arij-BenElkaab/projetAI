import logging
import requests

# -----------------------------
# Configure logging
# -----------------------------
logging.basicConfig(
    filename='chatbot_anomalies.log',
    level=logging.WARNING,
    format='%(asctime)s - %(levelname)s - %(message)s'
)

# -----------------------------
# Function to get available trips from backend
# -----------------------------
def get_available_trajets(departure, arrival):
    """
    Call the Spring Boot API to get available trips between departure and arrival
    """
    url = "http://localhost:8089/trajets/search"  # replace with your API URL
    payload = {"departure": departure, "arrival": arrival}
    try:
        response = requests.post(url, json=payload)
        response.raise_for_status()
        return response.json()  # Should return a list of available trips
    except Exception as e:
        logging.error(f"Error calling backend API: {e}")
        return []

# -----------------------------
# Function to check LLaMA response
# -----------------------------
def check_llama_response(llama_response):
    """
    Validate the trips suggested by LLaMA against backend data.
    Logs anomalies where trips do not exist.
    """
    anomalies = []

    for suggested_trajet in llama_response.get("trajets_suggérés", []):
        departure = suggested_trajet.get("departure")
        arrival = suggested_trajet.get("arrival")
        
        # Skip if essential info missing
        if not departure or not arrival:
            anomalies.append(suggested_trajet)
            continue

        # Check backend for available trips
        available_trips = get_available_trajets(departure, arrival)

        if not available_trips:
            anomalies.append(suggested_trajet)

    if anomalies:
        logging.warning(f"Anomalies detected in LLaMA response: {anomalies}")

    return anomalies

# -----------------------------
# Example usage
# -----------------------------
if __name__ == "__main__":
    # Example LLaMA response
    llama_response_example = {
        "trajets_suggérés": [
            {"departure": "Tunis", "arrival": "Sousse", "time": "08:00"},
            {"departure": "Tunis", "arrival": "Marsaxlokk", "time": "09:00"}  # likely anomaly
        ]
    }

    anomalies_found = check_llama_response(llama_response_example)
    if anomalies_found:
        print("Anomalies detected! Check 'chatbot_anomalies.log' for details.")
    else:
        print("No anomalies detected.")
