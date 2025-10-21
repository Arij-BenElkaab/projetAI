from flask import Flask, request, jsonify
from transformers import AutoTokenizer, AutoModelForCausalLM
import torch
import requests

app = Flask(__name__)

print("🔹 Loading LLaMA 3.2 3B model on CPU, please wait...")
MODEL_NAME = "meta-llama/Llama-3.2-3B"

tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
model = AutoModelForCausalLM.from_pretrained(
    MODEL_NAME,
    device_map=None,
    torch_dtype=torch.float32,
    low_cpu_mem_usage=True
)
model.to("cpu")

# --- Backend URLs ---
NLP_SERVER_URL = "http://localhost:5000/extract"
SPRING_BOOT_SEARCH_URL = "http://localhost:8089/api/trajets/search"
SPRING_BOOT_RESERVE_URL = "http://localhost:8089/api/trajets"

# --- Rule-based responses ---
GREETINGS = ["hello", "hi", "hey", "good morning", "good afternoon"]
THANKS = ["thank", "thanks", "thx", "merci"]

# --- Session store for interactive conversation ---
sessions = {}

@app.route("/generate", methods=["POST"])
def generate():
    data = request.json
    user_input = data.get("prompt", "").strip()
    session_id = data.get("sessionId")
    user_input_lower = user_input.lower()

    # --- Rule-based greetings / thanks ---
    if any(g in user_input_lower for g in GREETINGS):
        return jsonify({
            "response": "Hello! 👋 I'm TrajetChat, your travel assistant. Where would you like to go?",
            "trajets": [],
            "reservation": None
        })
    if any(t in user_input_lower for t in THANKS):
        return jsonify({
            "response": "You're welcome! 😊 Do you want to book a trip?",
            "trajets": [],
            "reservation": None
        })

    # --- Call NLP extraction ---
    nlp_response = requests.post(NLP_SERVER_URL, json={"text": user_input, "sessionId": session_id}).json()
    depart = nlp_response.get("depart")
    arrivee = nlp_response.get("arrivee")
    places = nlp_response.get("places", 1)
    intent = nlp_response.get("intent", "search")
    index = nlp_response.get("index", 0)
    strategy = nlp_response.get("strategy")

    trajets = []
    reservation_info = None

    # --- Search trips ---
    if intent == "search" and depart and arrivee:
        spring_resp = requests.post(SPRING_BOOT_SEARCH_URL, json={
            "depart": depart, "arrivee": arrivee, "places": places
        })
        if spring_resp.status_code == 200:
            trajets = spring_resp.json()
            # Apply strategy if requested
            if strategy:
                if strategy.lower() == "cheapest":
                    trajets.sort(key=lambda t: t["trajet"]["prixBase"])
                elif strategy.lower() == "earliest":
                    trajets.sort(key=lambda t: t["trajet"]["heureDepart"])
        # Save session for future reservation
        sessions[session_id] = {"depart": depart, "arrivee": arrivee, "places": places, "last_search_results": trajets}

    # --- Handle reservation ---
    if intent == "reserve":
        last_search = sessions.get(session_id, {})
        trajets = last_search.get("last_search_results", [])
        if trajets:
            chosen_index = min(index, len(trajets)-1)
            chosen_trajet = trajets[chosen_index]
            reserve_payload = {"passagerId": "passengerId123", "places": places}
            reserve_resp = requests.post(
                f"{SPRING_BOOT_RESERVE_URL}/{chosen_trajet['trajet']['id']}/reserver",
                json=reserve_payload
            )
            if reserve_resp.status_code == 200:
                reservation_info = reserve_resp.json()
            else:
                reservation_info = {"error": "Reservation failed. Not enough seats."}

    # --- Build short summary for LLaMA ---
    if intent == "search":
        if trajets:
            summary = f"I found {len(trajets)} trips from {depart} → {arrivee}. You can type 'reserve first', 'reserve cheapest' or 'reserve earliest'."
        else:
            summary = f"Sorry, no trips found from {depart} → {arrivee}."
    elif intent == "reserve":
        if reservation_info and not reservation_info.get("error"):
            summary = f"✅ Reservation confirmed for {depart} → {arrivee}. Seats: {places}. Total: €{reservation_info['prixFinal']}."
        else:
            summary = f"❌ Reservation failed. {reservation_info.get('error', '')}"
    else:
        summary = "Hello! 👋 Where would you like to go?"

    # --- LLaMA conversational response ---
    prompt = f"You are TrajetChat, a friendly travel assistant.\nUser: {user_input}\nAssistant: {summary}"
    inputs = tokenizer(prompt, return_tensors="pt").to("cpu")
    outputs = model.generate(
        **inputs,
        max_new_tokens=100,
        do_sample=True,
        temperature=0.7,
        top_p=0.9,
        pad_token_id=tokenizer.eos_token_id
    )
    text = tokenizer.decode(outputs[0], skip_special_tokens=True)
    response_text = text.split("Assistant:")[-1].strip() or summary

    return jsonify({
        "response": response_text,
        "trajets": trajets,
        "reservation": reservation_info
    })


if __name__ == "__main__":
    app.run(port=5001, debug=True)
