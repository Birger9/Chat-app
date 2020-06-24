package com.example.tddd80project2;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * ProfileFragment handles different options the logged in user can choose between when they
 * visit an other user's profile page
 */
public class ProfileFragment extends Fragment {

    private final MainActivity main = new MainActivity();
    private String requestBody = null;
    private TextView locationView = null;

    public ProfileFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.profile_screen, container, false);
        final Button removeFriend = view.findViewById(R.id.removeFriendBut);
        final Button addFriendButton = view.findViewById(R.id.addFriendBut);
        final Button blockButton = view.findViewById(R.id.blockUserBut);
        locationView =  view.findViewById(R.id.locationTextView); // The location of the profile user will be shown here
        createJsonBody();

        Button chatButton = view.findViewById(R.id.chatBut);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                createChat();
            }
        });

        setProfileUsername(view); // Sets the profile username
        setFriendStatus(view, removeFriend, addFriendButton, blockButton); // Sets the status between two users
        setLocationText(); // Shows the tha last location of the user in the profile

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend(view, removeFriend, addFriendButton, blockButton); // Sends a friend request to the profile user

            }
        });

        Button backbutton = view.findViewById(R.id.backBut);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                fragmentManager.popBackStack(); // Goes back to the ChatList screen
            }
        });

        blockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeFriend(); // Removes PendingFriend data or Friend data from database
                blockUser(); // Blocks the user
                TextView friendInfo = view.findViewById(R.id.friendStatus);
                friendInfo.setText(R.string.blockedInfo);
                setAddFriendButtonUnclickable(addFriendButton);
                setRemoveFriendButtonUnclickable(removeFriend);
            }
        });
        return view;
    }

    /**
     * setProfileUsername the username of the user in the profile
     * @param View the view.
     */
    private void setProfileUsername(View view) {
        TextView profileUsername = view.findViewById(R.id.profileUsernameView);
        profileUsername.setText(main.getSearchedUsername());
    }

    /**
     * setFriendStatus sets the status between the user in the profile and the logged in user
     * @param View the view, Button removeFriend, Button addFriend, Button blockButton.
     */
    private void setFriendStatus(View view, Button removeFriend, Button addFriend, Button blockButton) {
        TextView friendInfo = view.findViewById(R.id.friendStatus);
        if(main.getBlockedOrNot()){ // true if blocked
            friendInfo.setText(R.string.profileBlocked);
            setAddFriendButtonUnclickable(addFriend);
            setRemoveFriendButtonUnclickable(removeFriend);
            setBlockButtonUnclickable(blockButton);
        }
        else if(main.getFriendsOrNot()) { // true if they are friends
            friendInfo.setText(R.string.friends_yes);
            setAddFriendButtonUnclickable(addFriend);
        }
        else if(main.getPendingFriends()) { // True if friend request is sent
            friendInfo.setText(R.string.pending);
            setAddFriendButtonUnclickable(addFriend);
            setRemoveFriendButtonUnclickable(removeFriend);
        }else{
            friendInfo.setText(R.string.friends_no);
            setRemoveFriendButtonUnclickable(removeFriend);
        }
    }

    /**
     * setAddFriendButtonUnclickable sets the add friend button unclickable.
     * @param Button addFriend.
     */
    private void setAddFriendButtonUnclickable(Button addFriend) {
        addFriend.setEnabled(false);
    }

    /**
     * setRemoveFriendButtonUnclickable sets the remove friend button unclickable.
     * @param Button removeFriend.
     */
    private void setRemoveFriendButtonUnclickable(Button removeFriend) {
        removeFriend.setEnabled(false);
    }

    /**
     * setBlockButtonUnclickable sets the block button unclickable.
     * @param Button blockUser, a button.
     */
    private void setBlockButtonUnclickable(Button blockUser) {
        blockUser.setEnabled(false);
    }

    /**
     * createJsonBody creates a jSONObject that contains the searched user's userid
     */
    private void createJsonBody() {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("userid", main.getSearchedUserId());
            requestBody = jsonBody.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * addFriend adds the user in the profile as a friend to the user that is logged in to the app
     * @param View the view, Button removeFriend, Button addFriend, Button blockButton.
     */
    private void addFriend(final View view, final Button removeFriend, final Button addFriendButton, final Button blockButton) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getAddFriendUrl(), new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                main.setPendingFriends(true);
                setFriendStatus(view,  removeFriend, addFriendButton, blockButton); // Sets the friend status as sent
                setAddFriendButtonUnclickable(addFriendButton); // Sets the "Add friend" button unclickable
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
     * blockUser blocks the user in the profile.
     */
    private void blockUser() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getBlockFriendUrl(), new Response.Listener<String>() {
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
     * removeFriend removes the user in the profile as friend.
     */
    private void removeFriend() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getRemoveFriendUrl(), new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {}
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY ERROR", error.toString());
            }
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
     * goToChat the logged in user is sent to the chat with the user in the profile.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void goToChat() {
        ChatScreen chatScreen = new ChatScreen();
        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        // Add the fragment to the 'contentFragment' FrameLayout
        ft.replace(R.id.contentFragment, chatScreen); // Return to the login screen
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * createChat the logged in user starts a chat with the user in the profile.
     */
    private void createChat(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getCreateChat(), new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                Map mapChatInfo = new Gson().fromJson(response, Map.class); // retrieves or creates a chatid, depending if the chat existed before or not
                double aChatID = (double) mapChatInfo.get("chatid");
                int chatID = (int) aChatID;
                main.setCurrentChatID(chatID);
                goToChat(); // Goes to the fragment ChatScreen
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
     * setLocationText sets the location of the user in the profile.
     */
    private void setLocationText() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getGetLocUrl(), new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                Map locationMap = new Gson().fromJson(response, Map.class); // Contains chat id and if the chat already existed
                String location = (String) locationMap.get("location");
                locationView.setText(location);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("VOLLEY ERROR", error.toString());
            }
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
}
