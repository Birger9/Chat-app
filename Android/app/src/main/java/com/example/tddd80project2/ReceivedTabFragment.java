package com.example.tddd80project2;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
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
 * ReceivedReqDialog keeps track of all the recieved friend requests
 */
public class ReceivedTabFragment extends ListFragment {

    private final MainActivity main = new MainActivity();
    private static List receivedFriends;
    private int mStackLevel = 0;
    private String requestBody = null;

    public ReceivedTabFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sent_tab, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getReceivedFriendRequests(); // Gets all the received friend requests
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        getClickedUserId(receivedFriends.get(position).toString());
        showPopupDialog();
    }

    /**
     * showPopupDialog shows a popup dialog which the logged in user can interact with.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showPopupDialog() {
        mStackLevel++;

        // main.getDialogPopup().show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = Objects.requireNonNull(getFragmentManager()).beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("receivedDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        main.setDialogPopup(ReceivedReqDialog.newInstance(mStackLevel));
        main.getDialogPopup().show(ft, "dialog");
    }

    /**
     * getClickedUserId shows a popup dialog which the logged in user can interact with.
     * @param String username, the username which you want to retrieve their userid.
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
                main.setClickedUserId(intUserID);
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
     * getReceivedFriendRequests gets all the received friend requests and stores them in a
     * scrollable list. Which the logged user can interact with.
     */
    private void getReceivedFriendRequests() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, main.getReceivedFriendRequestsUrl(), new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                if(response != null) {
                    Map receivedFriendMap = new Gson().fromJson(response, Map.class); // Contains all the received friend requests
                    receivedFriends = (List) receivedFriendMap.get("received");
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(Objects.requireNonNull(getActivity()), android.R.layout.simple_list_item_1, receivedFriends);
                    setListAdapter(adapter);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {Log.e("VOLLEY ERROR", error.toString());}
        }) {

            @Override
            public Map<String, String> getHeaders() {
                return main.getUserHeader();
            }
        };
        main.getQueue().add(stringRequest);
    }

    /**
     * createJsonBody creates a jSONObject that contains the clicked user's username.
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
