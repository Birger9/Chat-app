package com.example.tddd80project2;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.ListFragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * ChatScreen handles everything that has to do with chatting between two people for example
 * updating the chat every 5 seconds
 */
public class ChatScreen extends ListFragment {

    private final MainActivity main = new MainActivity();
    private String requestBody = null;
    private static List chatMessages;
    private final Timer timer = new Timer();
    private static String theMessage = "";
    private static boolean chatActive;

    public ChatScreen() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.chat_screen, container, false);
        ImageButton sendMessage =  view.findViewById(R.id.sendButton);
        Button backButton = view.findViewById(R.id.backButton);
        chatActive = true; // Sets that the chat is active
        backButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                chatActive = false;
                timer.cancel(); // Cancels the scheduled task
                timer.purge();
                FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                fragmentManager.popBackStack();
            }
        });
        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText messageEditText = view.findViewById(R.id.messageText); // Contains the message the user wants to send
                theMessage = messageEditText.getText().toString(); // Sets the message that will be used in the JSonBody
                createJsonBody();
                sendTheMessage();
                messageEditText.setText(""); // Clears the message bar where the user types their message
                callAsynchronousTask(); // Updates the chat
            }
        });
        createJsonBody(); // So that the retrieved chatID is saved
        callAsynchronousTask(); // Updates the chat
        return view;
    }

    /**
     * getChatMessages retrieves all the messages in an active chat and which user sent it.
     */
    private void getChatMessages() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getChatMessagesUrl(), new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                Map mapChatMessages = new Gson().fromJson(response, Map.class);
                chatMessages = (List) mapChatMessages.get("messages");
                List messages = new ArrayList(); // Initalizes a list that will contain all the messages
                if(chatMessages.size() >= 1) { // A check that atleast one message has been sent in the chat
                    for (int i = 0; i < chatMessages.size(); i++) {
                        List currentMessageInfoList = (List) chatMessages.get(i); // Contains the message at index i and who's userid sent it at index 1
                        double userID = (double) currentMessageInfoList.get(1);
                        int intUserID = (int) userID;
                        if(intUserID == main.getSearchedUserId()) { // Its not the current users message but the one the user is chatting with
                            // This message shall show the others users username
                            messages.add(main.getSearchedUsername() + ": " + currentMessageInfoList.get(0));
                        }else{
                            //This message shall show the current users username(the users own message)
                            messages.add(main.getCurrentUsername() + ": " + currentMessageInfoList.get(0));
                        }
                    }
                    if(chatActive) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_1, messages);
                        setListAdapter(adapter);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {Log.e("VOLLEY ERROR", error.toString());}
        }) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public byte[] getBody() {
                return requestBody == null ? null : requestBody.getBytes(StandardCharsets.UTF_8);
            }
            @Override
            public Map<String, String> getHeaders() {
                return main.getUserHeader();
            }
        };
        main.getQueue().add(stringRequest);
    }

    /**
     * createJsonBody creates a jSONObject that contains the user's sent message, the chats chatid and the user's userid
     */
    private void createJsonBody() {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("message", theMessage);
            jsonBody.put("chatid", main.getCurrentChatID());
            jsonBody.put("userid", main.getSearchedUserId());
            requestBody = jsonBody.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * sendTheMessage sends a message and saves in the backend. So it can be retrieved by the method
     * getChatMessages later.
     */
    private void sendTheMessage() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getChatUrl(), new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {}
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {Log.e("VOLLEY ERROR", error.toString());}
        }) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public byte[] getBody() {
                return requestBody == null ? null : requestBody.getBytes(StandardCharsets.UTF_8);
            }
            @Override
            public Map<String, String> getHeaders() {
                return main.getUserHeader();
            }
        };
        main.getQueue().add(stringRequest);
    }

    /**
     * callAsynchronousTask updates the chat every 5 second while tha user is in the active chat
     */
    private void callAsynchronousTask() {
        final Handler handler = new Handler();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        getChatMessages(); // Updates the chat messages
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 5000); //execute every 5 seconds
    }}
