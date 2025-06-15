from flask import Flask, jsonify, request
import os
import json
from sync_core import iCloudNotesSync

app = Flask(__name__)
sync_core = iCloudNotesSync()
local_storage = os.path.join(os.path.expanduser("~"), "iCloudNotesSync")

@app.route('/api/login', methods=['POST'])
def login():
    data = request.json
    if not data or 'apple_id' not in data or 'password' not in data:
        return jsonify({"success": False, "error": "Missing credentials"}), 400
    
    success = sync_core.authenticate(data['apple_id'], data['password'])
    return jsonify({"success": success})

@app.route('/api/notes', methods=['GET'])
def get_notes():
    try:
        notes_file = os.path.join(local_storage, "notes.json")
        if os.path.exists(notes_file):
            with open(notes_file, 'r') as f:
                notes = json.load(f)
            return jsonify({"success": True, "notes": notes})
        else:
            return jsonify({"success": False, "error": "No synced notes found"}), 404
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

@app.route('/api/sync', methods=['POST'])
def sync():
    try:
        notes = sync_core.sync_to_local(local_storage)
        return jsonify({"success": True, "count": len(notes)})
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)