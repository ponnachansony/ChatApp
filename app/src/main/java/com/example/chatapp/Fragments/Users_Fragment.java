//package com.example.chatapp.Fragments;
//
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.chatapp.Adapters.UserAdapter;
//import com.example.chatapp.Home_chat_List;
//import com.example.chatapp.R;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.ArrayList;
//
//
//public class Users_Fragment extends Fragment implements Home_chat_List.OnSearchQueryListener {
//FirebaseAuth firebaseAuth;
//    FirebaseUser firebaseUser;
//    private RecyclerView recyclerView;
//    private UserAdapter userAdapter;
//    private ArrayList<com.example.chatapp.Model.FirebaseUser> users;
//
//    public static final String TAG = "Log: " + Users_Fragment.class.getSimpleName();
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_users_, container, false);
//
//        recyclerView = view.findViewById(R.id.user_recyclerview);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        users = new ArrayList<>();
//        userAdapter = new UserAdapter(requireContext());
//        recyclerView.setAdapter(userAdapter);
//        return view;
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        readUsersList();
//    }
//
//    private void readUsersList() {
//        firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("logged_in_user_cred");
//        reference.keepSynced(true);
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                users.clear();
//                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
//
//                    com.example.chatapp.Model.FirebaseUser newUser = com.example.chatapp.Model.FirebaseUser.fromDatabase(snapshot1);
//
//                    Log.d(TAG, "onDataChange: " + newUser);
//
//                    if (!firebaseUser.getUid().equals(newUser.getUuid())) {
//                        users.add(newUser);
//                    }
//                }
//                Log.d("UsersFragment", "Number of users: " + users.size());
//                userAdapter.setItems(users);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("UsersFragment", "Database error: " + error.getMessage());
//            }
//        });
//    }
//
//
//    @Override
//    public void onQuerySubmit(String query) {
//        userAdapter.filter(query);
//    }
//
//
//}
//
//
package com.example.chatapp.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Adapters.UserAdapter;
import com.example.chatapp.Home_chat_List;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Users_Fragment extends Fragment implements Home_chat_List.OnSearchQueryListener {
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private ArrayList<com.example.chatapp.Model.FirebaseUser> users;

    public static final String TAG = "Log: " + Users_Fragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_, container, false);

        recyclerView = view.findViewById(R.id.user_recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        users = new ArrayList<>();
        userAdapter = new UserAdapter(requireContext());
        recyclerView.setAdapter(userAdapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        readUsersList();
    }

    private void readUsersList() {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("logged_in_user_cred");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.clear();
                ArrayList<com.example.chatapp.Model.FirebaseUser> onlineUsers = new ArrayList<>();

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    com.example.chatapp.Model.FirebaseUser newUser = com.example.chatapp.Model.FirebaseUser.fromDatabase(snapshot1);

                    if (!firebaseUser.getUid().equals(newUser.getUuid())) {
                        users.add(newUser);

                        // Check if the user is online and add them to the onlineUsers list
                        if (newUser.getOnline() != null && newUser.getOnline()) {
                            onlineUsers.add(newUser);
                        }
                    }
                }

                // Sort the users list to display online users at the top
                users.sort((user1, user2) -> {
                    boolean online1 = onlineUsers.contains(user1);
                    boolean online2 = onlineUsers.contains(user2);

                    if (online1 && !online2) {
                        return -1;
                    } else if (!online1 && online2) {
                        return 1;
                    } else {
                        return 0;
                    }
                });

                Log.d("UsersFragment", "Number of users: " + users.size());
                userAdapter.setItems(users);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UsersFragment", "Database error: " + error.getMessage());
            }
        });
    }

    @Override
    public void onQuerySubmit(String query) {
        userAdapter.filter(query);
    }
}
