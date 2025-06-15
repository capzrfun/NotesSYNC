package com.example.icloudnotessync;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText appleIdInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button syncButton;
    private RecyclerView notesRecyclerView;
    private NotesAdapter notesAdapter;
    private List<Note> notesList = new ArrayList<>();
    
    private RequestQueue requestQueue;
    private String apiBaseUrl = "http://192.168.1.100:5000/api"; // Change to your server IP

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        requestQueue = Volley.newRequestQueue(this);
        
        appleIdInput = findViewById(R.id.appleIdInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        syncButton = findViewById(R.id.syncButton);
        notesRecyclerView = findViewById(R.id.notesRecyclerView);
        
        notesAdapter = new NotesAdapter(notesList);
        notesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notesRecyclerView.setAdapter(notesAdapter);
        
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncNotes();
            }
        });
    }
    
    private void login() {
        String appleId = appleIdInput.getText().toString();
        String password = passwordInput.getText().toString();
        
        if (appleId.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both Apple ID and password", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("apple_id", appleId);
            jsonBody.put("password", password);
            
            JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                apiBaseUrl + "/login",
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                fetchNotes();
                            } else {
                                Toast.makeText(MainActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            );
            
            requestQueue.add(request);
        } catch (JSONException e) {
            Toast.makeText(this, "Error creating request", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void syncNotes() {
        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.POST,
            apiBaseUrl + "/sync",
            null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            int count = response.getInt("count");
                            Toast.makeText(MainActivity.this, "Synced " + count + " notes", Toast.LENGTH_SHORT).show();
                            fetchNotes();
                        } else {
                            Toast.makeText(MainActivity.this, "Sync failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        requestQueue.add(request);
    }
    
    private void fetchNotes() {
        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET,
            apiBaseUrl + "/notes",
            null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        boolean success = response.getBoolean("success");
                        if (success) {
                            JSONArray notesArray = response.getJSONArray("notes");
                            notesList.clear();
                            
                            for (int i = 0; i < notesArray.length(); i++) {
                                JSONObject noteObj = notesArray.getJSONObject(i);
                                Note note = new Note(
                                    noteObj.getString("id"),
                                    noteObj.getString("title"),
                                    noteObj.getString("content"),
                                    noteObj.getString("updated")
                                );
                                notesList.add(note);
                            }
                            
                            notesAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to fetch notes", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(MainActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MainActivity.this, "Network error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        requestQueue.add(request);
    }
}