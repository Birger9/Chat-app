package com.example.tddd80project2;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Objects;


/**
 * RegisterUserFragment handles the register page
 */
public class RegisterUserFragment extends Fragment {

    private final MainActivity main = new MainActivity();
    private String requestBody = null;
    public RegisterUserFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.register_screen, container, false);
        Button registerButton = view.findViewById(R.id.createUser);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText usernameText = view.findViewById(R.id.usernameText);
                String username = usernameText.getText().toString(); // The inputted username

                EditText mailText = view.findViewById(R.id.mailText);
                String mail = mailText.getText().toString(); // The inputted mail

                EditText passwordText = view.findViewById(R.id.passwordText);
                String password = passwordText.getText().toString(); // The inputted password

                createJsonBody(username, mail, password);

                // Check if inputted email is valid
                if (isEmailValid(mail)) {
                    registerUser(view);
                }else {  // the inputted email is not valid
                    TextView userTakenView = view.findViewById(R.id.showUserTaken);
                    userTakenView.setText(R.string.validemail);
                }


                usernameText.setText(""); // Clears the username bar
                mailText.setText(""); // Clears the email bar
                passwordText.setText(""); // Clears the password bar

            }
        });

        Button backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                fragmentManager.popBackStack();
            }
        });
        return view;
    }

    /**
     * createJsonBody creates a jSONObject that contains a inputted username, mail and password
     * @param String username, inputted username. String mail, inputted mail.
     * String password, inputted password.
     */
    private void createJsonBody(String username, String mail, String password) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("email", mail);
            jsonBody.put("password", password);
            requestBody = jsonBody.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * registerUser registers a user and save their information in the backend database.
     * @param View view, the view.
     */
    private void registerUser(final View view) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getRegisterUrl(), new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                fragmentManager.popBackStack(); // Goes back to the login screen
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 409) { // Return 409 if username is taken
                    TextView userTakenView = view.findViewById(R.id.showUserTaken);
                    userTakenView.setText(R.string.username_taken);
                }
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

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {responseString = String.valueOf(response.statusCode);}
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(Objects.requireNonNull(response)));
            }
        };
        main.getQueue().add(stringRequest);
    }

    /**
     * isEmailValid checks if inputted email is valid.
     * @param CharSequence email, an inputted mail.
     * @return boolean, true if valid mail otherwise false.
     */
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
