package com.example.tddd80project2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * MainActivity keeps track about different information about users and urls to the backend.
 * But also gets the location of an user and sets it in an variable that can be accessed later.
 */
@SuppressWarnings("SameReturnValue")
public class MainActivity extends AppCompatActivity {

    private static RequestQueue queue = null;
    private static Map<String, String> userHeader = null;
    private static String currentUsername = null;
    private static String searchedUsername = null;
    private static int searchedUserId = 1; // The user's user id you searched for in ChatListFragment
    private static int clickedUserId = 1;
    private static int currentChatID = 1;
    private static Boolean friendsOrNot = null; // Used in ProfileFragment, set in ChatlistFragment
    private static Boolean pendingFriends = null; // Used in ProfileFragment, set in ChatlistFragment
    private static Boolean blockedOrNot = null; // Used in ProfileFragment, set in ChatlistFragment
    private static DialogFragment dialogPopup;
    private Geocoder geocoder = null;
    private static String currentAddress = null;

    @SuppressLint("StaticFieldLeak")
    private static FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());
        setTheLocation(); // Sets the address for when the user started the app

        LoginScreenFragment loginScreenFragment = new LoginScreenFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Add the fragment to the 'contentFragment' FrameLayout
        ft.add(R.id.contentFragment, loginScreenFragment);
        ft.commit();
    }

    /**
     * setTheLocation sets the location of the user that opened the app
     */
    public void setTheLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED) {
                // The user has granted that their location can be retrieved
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                if (location != null) {
                                    try {
                                        addressFromLocation(location);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
            }
        }
    }

    /**
     * addressFromLocation creates a string from information from a Location object.
     * @param Location contains location information.
     */
    private void addressFromLocation(Location location) throws IOException {
        List<Address> addresses;
        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String theAddress = address + ", " + city;
        setCurrentAddress(theAddress); // Saves the user's location that used the app
    }

    public RequestQueue getQueue() {
        return queue;
    }

    public Map<String, String> getUserHeader() {
        return userHeader;
    }

    public void setUserHeader(Map<String, String> userHeader) {MainActivity.userHeader = userHeader; }

    public String getSearchedUsername() {
        return searchedUsername;
    }

    public void setSearchedUsername(String searchedUsername) {MainActivity.searchedUsername = searchedUsername; }

    public int getSearchedUserId() {
        return searchedUserId;
    }

    public void setSearchedUserId(int searchedUserId) {MainActivity.searchedUserId = searchedUserId; }

    public Boolean getFriendsOrNot() {
        return friendsOrNot;
    }

    public void setFriendsOrNot(Boolean friendsOrNot) {
        MainActivity.friendsOrNot = friendsOrNot;
    }

    public Boolean getBlockedOrNot() {
        return blockedOrNot;
    }

    public void setBlockedOrNot(Boolean blockedOrNot) {
        MainActivity.blockedOrNot = blockedOrNot;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public void setCurrentUsername(String currentUsername) {MainActivity.currentUsername = currentUsername; }

    public Boolean getPendingFriends() {
        return pendingFriends;
    }

    public void setPendingFriends(Boolean pendingFriends) {MainActivity.pendingFriends = pendingFriends;}

    public DialogFragment getDialogPopup() {
        return dialogPopup;
    }

    public void setDialogPopup(DialogFragment dialogPopup) {MainActivity.dialogPopup = dialogPopup; }

    public int getClickedUserId() {
        return clickedUserId;
    }

    public void setClickedUserId(int clickedUserId) {
        MainActivity.clickedUserId = clickedUserId;
    }

    public int getCurrentChatID() {
        return currentChatID;
    }

    public void setCurrentChatID(int currentChatID) {
        MainActivity.currentChatID = currentChatID;
    }

    public String getGetBlockedUsersUrl() {
        return "https://projectoinker.herokuapp.com/getallblocked";
    }

    public String getGetUserIdUrl() {
        return "https://projectoinker.herokuapp.com/getuserid";
    }

    public String getLogoutUrl() {
        return "https://projectoinker.herokuapp.com/user/logout";
    }

    public String getFindUserUrl() {
        return "https://projectoinker.herokuapp.com/searchforuser";
    }

    public String getChatMessagesUrl() {
        return "https://projectoinker.herokuapp.com/chatmessages";
    }

    public String getChatUrl() {
        return "https://projectoinker.herokuapp.com/chat";
    }

    public String getLoginUrl() {
        return "https://projectoinker.herokuapp.com/user/login";
    }

    public String getAddFriendUrl() {
        return "https://projectoinker.herokuapp.com/user/addfriend";
    }

    public String getBlockFriendUrl() {
        return "https://projectoinker.herokuapp.com/block";
    }

    public String getRemoveFriendUrl() {
        return "https://projectoinker.herokuapp.com/removefriend";
    }

    public String getCreateChat() {
        return "https://projectoinker.herokuapp.com/create_chat";
    }

    public String getAcceptFriendUrl() {
        return "https://projectoinker.herokuapp.com/user/acceptfriend";
    }

    public String getDeclineFriendUrl() {
        return "https://projectoinker.herokuapp.com/user/declinepending";
    }

    public String getReceivedFriendRequestsUrl() {
        return "https://projectoinker.herokuapp.com/pending/received";
    }

    public String getRegisterUrl() {
        return "https://projectoinker.herokuapp.com/register";
    }

    public String getCancelPendingFriendUrl() {
        return "https://projectoinker.herokuapp.com/user/cancelpending";
    }

    public String getSentFriendRequestsUrl() {
        return "https://projectoinker.herokuapp.com/pending/sent";
    }

    public String getUnblockUserUrl() {
        return "https://projectoinker.herokuapp.com/unblock";
    }

    public  String getAllChatsUrl() {
        return "https://projectoinker.herokuapp.com/chats"; }

    public String getSetLocUrl() {
        return "https://projectoinker.herokuapp.com/setlocation"; }

    public String getDeleteLocUrl() {
        return "https://projectoinker.herokuapp.com/deletelocation"; }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {MainActivity.currentAddress = currentAddress; }

    public String getGetLocUrl() {
        return "https://projectoinker.herokuapp.com/getlocation";
    }
}
