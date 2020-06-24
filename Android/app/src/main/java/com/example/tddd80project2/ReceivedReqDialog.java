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
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * ReceivedReqDialog handles the popup dialog options when the user clicks on a recieved friend request
 */
public class ReceivedReqDialog extends DialogFragment {

    private final MainActivity main = new MainActivity();
    private String requestBody = null;

    public ReceivedReqDialog() {

    }

    /**
     * Create a new instance of ReceivedReqDialog, providing "num"
     * as an argument.
     */
    static ReceivedReqDialog newInstance(int num) {
        ReceivedReqDialog f = new ReceivedReqDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.received_req_popup, container, false);

        TextView closeDialog = view.findViewById(R.id.closeDialogText);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissPopup();
            }
        });

        Button acceptFriendButton = view.findViewById(R.id.acceptFriendButton);
        acceptFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createJsonBody(); // Contains info about the clicked Friend request
                declinePendingFriend(); // Removes the request from Pending Friend in the database
                acceptFriendRequest(); // Adds the person to the Friend in database
                dismissPopup();
            }
        });

        Button declineFriendButton = view.findViewById(R.id.cancelFriendReqBut);
        declineFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createJsonBody(); // Contains info about the clicked Friend request
                declinePendingFriend(); // Removes the request from Pending Friend in the database
                dismissPopup();
            }
        });

        Button blockPerson = view.findViewById(R.id.unblockButton);
        blockPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createJsonBody(); // Contains info about the clicked Friend request
                declinePendingFriend(); // Removes the request from Pending Friend in the database
                blockPendingFriend(); // Blocks the person
                dismissPopup();
            }
        });
        return view;
    }

    /**
     * createJsonBody creates a jSONObject that contains the clicked user's userid
     */
    private void createJsonBody() {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("userid", main.getClickedUserId());
            requestBody = jsonBody.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * dismissPopup closes the popup
     */
    private void dismissPopup() {
        main.getDialogPopup().dismiss(); // Closes the popup
    }

    /**
     * acceptFriendRequest accepts a friend request.
     */
    private void acceptFriendRequest() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getAcceptFriendUrl(), new Response.Listener<String>() {
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
     * declinePendingFriend declines a friend request.
     */
    private void declinePendingFriend() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getDeclineFriendUrl(), new Response.Listener<String>() {
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
     * blockPendingFriend declines a friend request and blocks the person that sent it.
     */
    private void blockPendingFriend() {
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
}
