package com.example.tddd80project2;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.ListFragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ChatListFragment is the home screen where you can see active chats and search for users. But also
 * click to log out, go to the friend request page or the blocked page
 */
public class ChatListFragment extends ListFragment {

    private String requestBody = null;
    private static List chats;
    private final MainActivity main = new MainActivity();

    public ChatListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_chat_screen, container, false);

        getActiveChats(); // Gets all active Chats the user has

        ImageView logoutImage = view.findViewById(R.id.imageLogout);
        logoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnToLoginScreen();
            }
        });

        ImageView friendRequestBut = view.findViewById(R.id.imageFriendRequest);
        friendRequestBut.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                goToFriendRequestScreen();
            }
        });

        ImageView blockedImage = view.findViewById(R.id.imageBlockFriend);
        blockedImage.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                goToBlockedScreen();
            }
        });

        final SearchView searchView = view.findViewById(R.id.searchNameView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                final String searchedUsername = searchView.getQuery().toString();
                createJsonBody(searchedUsername);
                findUser(searchedUsername);
                return true; // For onQueryTextSubmit
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return view;
    }

    /**
     * getActiveChats retrieves all the active chats the user has and sets them in a scrollable list.
     */
    private void getActiveChats() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, main.getAllChatsUrl(), new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                Map mapChatMessages = new Gson().fromJson(response, Map.class);
                chats = (List) mapChatMessages.get("list");
                if(chats != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_1, chats);
                    setListAdapter(adapter);
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
     * returnToLoginScreen returns the user to the Login-screen
     */
    private void returnToLoginScreen() {
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, main.getLogoutUrl(), new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                LoginScreenFragment loginScreenFragment = new LoginScreenFragment();
                FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();

                // Add the fragment to the 'contentFragment' FrameLayout
                ft.replace(R.id.contentFragment, loginScreenFragment); // Return to the login screen
                ft.addToBackStack(null);
                ft.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {Log.e("VOLLEY ERROR", error.toString());}
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public Map<String, String> getHeaders() {
                return main.getUserHeader();
            }
        };
        main.getQueue().add(stringRequest);
    }

    /**
     * goToFriendRequestScreen creates a FriendRequestFragment so that the user can see their sent and received friend requests
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void goToFriendRequestScreen() {
        FriendRequestFragment friendRequest = new FriendRequestFragment();
        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        // Add the fragment to the 'contentFragment' FrameLayout
        ft.replace(R.id.contentFragment, friendRequest);
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * createJsonBody creates a jSONObject that contains the searched user's username
     * @param String contains a searched user's username
     */
    private void createJsonBody(String searchedUsername) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", searchedUsername);
            requestBody = jsonBody.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * findUser finds a searched user and goes to their profile page
     * @param String the searched username
     */
    private void findUser(final String searchedUsername) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getFindUserUrl(), new Response.Listener<String>() {
            @SuppressWarnings("ConstantConditions")
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                main.setSearchedUsername(searchedUsername);
                if(!main.getCurrentUsername().equals(main.getSearchedUsername())) { //So that you do not search on yourself
                    Map mapUserInfo = new Gson().fromJson(response, Map.class); // Contains the searched users userid and if they are friends or not

                    double userID = (double) mapUserInfo.get("userid");
                    int intUserID = (int) userID;
                    boolean friendStatus = (Boolean) mapUserInfo.get("friends");
                    boolean pendingStatus = (Boolean) mapUserInfo.get("pending");
                    boolean blockedStatus = (Boolean) mapUserInfo.get("blocked");
                    main.setSearchedUserId(intUserID);
                    main.setFriendsOrNot(friendStatus); // Sets friend status between the user and the searched user
                    main.setPendingFriends(pendingStatus); // Sets true or false depending if a friend request is sent or not
                    main.setBlockedOrNot(blockedStatus); // Sets true or false depending if the searched user is blocked or not

                    ProfileFragment profileFragment = new ProfileFragment();
                    FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                    FragmentTransaction ft = fragmentManager.beginTransaction();

                    // replace the fragment to the 'contentFragment' FrameLayout
                    ft.replace(R.id.contentFragment, profileFragment);
                    ft.addToBackStack(null);
                    ft.commit();
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
     * goToBlockedScreen goes to the blocked Screen (BlockedFragment object)
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void goToBlockedScreen() {
        BlockedFragment blockedFragment = new BlockedFragment();
        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        // Add the fragment to the 'contentFragment' FrameLayout
        ft.replace(R.id.contentFragment, blockedFragment);
        ft.addToBackStack(null);
        ft.commit();
    }
}
