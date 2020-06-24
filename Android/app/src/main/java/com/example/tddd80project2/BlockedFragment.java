package com.example.tddd80project2;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
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
 * BlockedFragment handles the view when you want to see all the current blocked users
 */
public class BlockedFragment extends ListFragment {
    private final MainActivity main = new MainActivity();
    private static List blockedUsers;
    private int mStackLevel = 0;
    private String requestBody = null;

    public BlockedFragment() {

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        getClickedUserId(blockedUsers.get(position).toString());
        showPopupDialog();
    }

    /**
     * showPopupDialog creates an a popup that the user can interact with
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showPopupDialog() {
        mStackLevel++;

        FragmentTransaction ft = Objects.requireNonNull(getFragmentManager()).beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        main.setDialogPopup(UnblockedDialog.newInstance(mStackLevel));
        main.getDialogPopup().show(ft, "unblockedDialog");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.blocked_screen, container, false);
        getAllBlockedUsers(); // Retrieves all the blocked users

        Button backButton = view.findViewById(R.id.returnButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                fragmentManager.popBackStack(); // Goes back to the ChatList screen
            }
        });
        return view;
    }

    /**
     * getAllBlockedUsers retrieves the current blocked users and adds them to a scrollable list
     */
    private void getAllBlockedUsers() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, main.getGetBlockedUsersUrl(), new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                if(response != null){
                    Map mapBlockedUsername = new Gson().fromJson(response, Map.class); // mapBlockedUsername contains all the blocked users usernames
                    blockedUsers = (List) mapBlockedUsername.get("blocked");
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_1, blockedUsers);
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

            @Override
            public Map<String, String> getHeaders() {
                return main.getUserHeader();
            }
        };
        main.getQueue().add(stringRequest);
    }

    /**
     * getClickedUserId retrieves and sets the userid from the user that was clicked on
     * @param String contains a clicked user's username
     */
    private void getClickedUserId(String username) {
        createJsonBody(username);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getGetUserIdUrl(), new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                Map mapUserId = new Gson().fromJson(response, Map.class); // Contains the clicked users userId
                double userID = (double) mapUserId.get("userid");
                int intUserID = (int) userID;
                main.setClickedUserId( intUserID);
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
     * createJsonBody creates a jSONObject that contains the clicked user's username
     * @param String contains a clicked user's username
     */
    private void createJsonBody(String username) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            requestBody = jsonBody.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
