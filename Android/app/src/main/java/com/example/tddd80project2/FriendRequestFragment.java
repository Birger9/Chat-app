package com.example.tddd80project2;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;


/**
 * FriendRequestFragment handles everything about the logged in user's received and sent friend
 * requests
 */
public class FriendRequestFragment extends Fragment {

    public FriendRequestFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.added_friends, container, false);

        Button backButton = view.findViewById(R.id.backFriendBut);
        backButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        TabLayout tabLayout = view.findViewById(R.id.friendrequest_tab);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()){
                    case 0:
                        createSentFriendRequestTabInfo();
                        return;
                    case 1:
                        createReceivedFriendRequestTabInfo();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        return view;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createSentFriendRequestTabInfo(); // So the user is met with a Sent tab, can be interacted by the user
    }

    /**
     * createSentFriendRequestTabInfo creates a Sent tab which the user can interact with
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createSentFriendRequestTabInfo() {
        SentTabFragment sentTabFragment = new SentTabFragment();
        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        // Add the fragment to the 'listContentFragment' FrameLayout
        ft.replace(R.id.tabFrameLayout, sentTabFragment);
        ft.commit();
    }

    /**
     * createReceivedFriendRequestTabInfo creates a Received tab which the user can interact with
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createReceivedFriendRequestTabInfo() {
        ReceivedTabFragment receivedTabFragment = new ReceivedTabFragment();
        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        // Add the fragment to the 'listContentFragment' FrameLayout
        ft.replace(R.id.tabFrameLayout, receivedTabFragment);
        ft.commit();
    }
}
