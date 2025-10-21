from flask import Flask, request, jsonify
import spacy, re, dateparser
from datetime import datetime

nlp = spacy.load("en_core_web_sm")
app = Flask(__name__)

# Cities list
cities = [
    "Tunis", "Ariana", "Ben Arous", "Manouba", "Nabeul", "Bizerte", "Beja", "Jendouba",
    "Kef", "Siliana", "Sousse", "Monastir", "Mahdia", "Sfax", "Kairouan", "Kasserine",
    "Sidi Bouzid", "Gabes", "Mednine", "Tataouine", "Gafsa", "Tozeur", "Kebili", "Zaghouan"
]

# Ordinal mapping
ordinal_map = {
    "first": 0, "1": 0,
    "second": 1, "2": 1,
    "third": 2, "3": 2,
    "fourth": 3, "4": 3
}

# --- Store last search per session ---
sessions = {}

@app.route("/extract", methods=["POST"])
def extract_request():
    data = request.json
    text = data.get("text", "")
    session_id = data.get("sessionId")  # <-- use session ID to track context
    text_lower = text.lower()
    doc = nlp(text)

    # --- Extract departure and arrival cities ---
    depart, arrivee = None, None
    for city in cities:
        city_lower = city.lower()
        if city_lower in text_lower:
            if not depart:
                depart = city
            elif city != depart:
                arrivee = city

    # --- Extract number of seats ---
    seats = 1
    match = re.search(r'(\d+)\s*seat', text, re.IGNORECASE)
    if match:
        seats = int(match.group(1))

    # --- Extract date/time ---
    date_time = dateparser.parse(text, settings={'PREFER_DATES_FROM': 'future'})
    date_time_iso = date_time.isoformat() if date_time else None

    # --- Extract latest arrival constraint ---
    latest_arrivee_iso = None
    match_time = re.search(r'before\s*(\d{1,2}(?::\d{2})?\s*(?:AM|PM)?)', text, re.IGNORECASE)
    if match_time and date_time:
        latest_time_parsed = dateparser.parse(match_time.group(1))
        latest_arrivee_dt = datetime.combine(date_time.date(), latest_time_parsed.time())
        latest_arrivee_iso = latest_arrivee_dt.isoformat()

    # --- Extract intent ---
    intent = "search"
    if any(w in text_lower for w in ["reserve", "book", "take"]):
        intent = "reserve"

    # --- Extract index ---
    index = 0
    match_index = re.search(r'\b(first|1|second|2|third|3|fourth|4)\b', text_lower)
    if match_index:
        word = match_index.group(1)
        index = ordinal_map.get(word, 0)

    # --- Extract strategy ---
    strategy = None
    if "earliest" in text_lower:
        strategy = "earliest"
    elif "cheapest" in text_lower:
        strategy = "cheapest"

    # --- Restore context from last search if reserve and depart/arrivee missing ---
    if session_id and intent == "reserve" and (not depart or not arrivee):
        last_search = sessions.get(session_id)
        if last_search:
            depart = last_search.get("depart")
            arrivee = last_search.get("arrivee")
            seats = last_search.get("places", seats)

    # --- Save context if a search is done ---
    if session_id and depart and arrivee and intent == "search":
        sessions[session_id] = {"depart": depart, "arrivee": arrivee, "places": seats}

    return jsonify({
        "depart": depart,
        "arrivee": arrivee,
        "dateTime": date_time_iso,
        "latestArriveeTime": latest_arrivee_iso,
        "places": seats,
        "intent": intent,
        "index": index,
        "strategy": strategy
    })

if __name__ == "__main__":
    app.run(port=5000)
