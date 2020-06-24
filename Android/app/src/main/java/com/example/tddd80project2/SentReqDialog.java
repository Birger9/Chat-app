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
 * SentReqDialog handles the popup dialog options when the user clicks on a sent friend request
 */
public class SentReqDialog extends DialogFragment {

    private final MainActivity main = new MainActivity();
    private String requestBody = null;

    public SentReqDialog() {

    }

    /**
     * Create a new instance of SentReqDialog, providing "num"
     * as an argument.
     */
    static SentReqDialog newInstance(int num) {
        SentReqDialog f = new SentReqDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.sent_req_popup, container, false);

        TextView closeDialog = view.findViewById(R.id.closeDialogText);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissPopup();
            }
        });

        Button cancelFriendButton = view.findViewById(R.id.cancelFriendReqBut);
        cancelFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createJsonBody(); // Contains info about the clicked Friend request
                cancelSentPendingFriend(); // Removes the request from Pending Friend in the database
                dismissPopup();
            }
        });

        Button blockPerson = view.findViewById(R.id.unblockButton);
        blockPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createJsonBody(); // Contains info about the clicked Friend request
                cancelSentPendingFriend(); // Removes the request from Pending Friend in the database
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
     * dismissPopup dismisses the popup
     */
    private void dismissPopup() {
        main.getDialogPopup().dismiss(); //Closes the popup
    }

    /**
     * cancelSentPendingFriend cancels the sent pending friend request.
     */
    private void cancelSentPendingFriend() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getCancelPendingFriendUrl(), new Response.Listener<String>() {
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
     * blockPendingFriend blocks the user the logged in user sent pending friend request to.
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
