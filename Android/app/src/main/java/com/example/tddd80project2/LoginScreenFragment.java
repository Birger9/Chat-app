package com.example.tddd80project2;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * LoginScreenFragment is where the user can log in or go to register screen
 */
public class LoginScreenFragment extends Fragment {

    private String requestBody = null;
    private final MainActivity main = new MainActivity();

    public LoginScreenFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.login_screen, container, false);
        final EditText usernameText = view.findViewById(R.id.username_login);
        final EditText passwordText = view.findViewById(R.id.password_login);

        Button registerButton = view.findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                clearEditTexts(usernameText, passwordText);
                goToRegisterScreen();
            }
        });

        Button loginButton = view.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameText.getText().toString();
                String password = passwordText.getText().toString();

                createJsonBody(username, password);

                loginUser(view);
            }
        });
        return view;
    }

    /**
     * goToRegisterScreen the user is sent to the Register Screen.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void goToRegisterScreen() {
        RegisterUserFragment registerUserFragment = new RegisterUserFragment();
        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        // Add the fragment to the 'contentFragment' FrameLayout
        ft.replace(R.id.contentFragment, registerUserFragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * createJsonBody creates a jSONObject that contains the logged in user's username, password
     * and location
     * @param String username contains the logged in user's username.
     * String password contains the logged in user's password
     */
    private void createJsonBody(String username, String password) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            main.setCurrentUsername(username); // So we know the name of the current user
            jsonBody.put("password", password);
            jsonBody.put("location", main.getCurrentAddress());
            requestBody = jsonBody.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * goToRegisterScreen the user is logged in the app and sent to the Chatlist Screen
     */
    private void loginUser(final View view) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getLoginUrl(), new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onResponse(String response) {
                Map mapToken = new Gson().fromJson(response, Map.class); // Contains the user token
                Map<String, String> header = new HashMap<>();
                header.put("Authorization", "Bearer " + mapToken.get("token"));
                main.setUserHeader(header);

                deleteOldAddress(); // Deletes the old user address/location saved in the backend

                setAddress(); // Saves the new user address/location in the backend

                ChatListFragment chatListFragment = new ChatListFragment();
                FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.replace(R.id.contentFragment, chatListFragment);
                ft.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse.statusCode == 401){ // Return 409 if username is not found
                    TextView userNotExist = view.findViewById(R.id.errorView);
                    userNotExist.setText(R.string.username_not_exist);
                }else if(error.networkResponse.statusCode == 409){
                    TextView wrongPassword = view.findViewById(R.id.errorView);
                    wrongPassword.setText(R.string.wrong_password);
                }
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
        };
        main.getQueue().add(stringRequest);
    }

    /**
     * clearEditTexts clears the username and password bars/EditTexts
     */
    private void clearEditTexts(EditText usernameText, EditText passwordText){
        usernameText.setText("");
        passwordText.setText("");
    }

    /**
     * deleteOldAddress deletes the user's previous signed position/location from the backend
     */
    private void deleteOldAddress() {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getDeleteLocUrl(), new Response.Listener<String>() {
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
     * setAddress sets the user's position/location
     */
    private void setAddress(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, main.getSetLocUrl(), new Response.Listener<String>() {
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
}
