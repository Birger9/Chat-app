package com.example.tddd80project2;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
 * UnblockedDialog handles the popup dialog options when the user clicks on a blocked user
 */
public class UnblockedDialog extends DialogFragment {

    private String requestBody = null;
    private final MainActivity main = new MainActivity();

    public UnblockedDialog() {

    }
    /**
     * Create a new instance of SentReqDialog, providing "num"
     * as an argument.
     */
    static UnblockedDialog newInstance(int num) {
        UnblockedDialog f = new UnblockedDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.unblocked_popup, container, false);

        Button unblock = view.findViewById(R.id.unblockButton);
        unblock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createJsonBody(); // Sets the payload with the clicked userid from BlockedFragment list
                unBlockUser(); // Unblocks a user
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
     * dismissPopup dismisses the popup.
     */
    private void dismissPopup() {
        main.getDialogPopup().dismiss(); //Closes the popup
    }

    /**
     * unBlockUser unblocks the clicked user.
     */
    private void unBlockUser() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getUnblockUserUrl(), new Response.Listener<String>() {
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
