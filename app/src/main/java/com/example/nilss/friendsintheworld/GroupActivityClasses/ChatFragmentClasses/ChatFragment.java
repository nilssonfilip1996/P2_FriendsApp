package com.example.nilss.friendsintheworld.GroupActivityClasses.ChatFragmentClasses;


import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.nilss.friendsintheworld.GroupActivityClasses.GroupController;
import com.example.nilss.friendsintheworld.GroupActivityClasses.ManageGroupFragmentClasses.ManageGroupsAdapter;
import com.example.nilss.friendsintheworld.OnCreateViewLIstener;
import com.example.nilss.friendsintheworld.Pojos.Message;
import com.example.nilss.friendsintheworld.Pojos.TextMessage;
import com.example.nilss.friendsintheworld.R;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";
    private GroupController groupController;
    private Button manageGroupsBtn, mapsBtn, sendMessageBtn, sendImagebtn;
    private EditText inputMessageEtv;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ChatAdapter adapter;
    private ArrayList<TextMessage> currentMessageList;
    private OnCreateViewLIstener mListener;

    public void onInit(OnCreateViewLIstener listener){
        this.mListener = listener;
    }


    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_chat, container, false);
        currentMessageList = new ArrayList<>();
        initRecycler(view);
        initComponents(view);
        return view;
    }

    private void initComponents(View view) {
        this.manageGroupsBtn = view.findViewById(R.id.manageGrpBtn);
        this.mapsBtn = view.findViewById(R.id.mapsBtn);
        this.sendMessageBtn = view.findViewById(R.id.sendMsgbtn);
        this.sendImagebtn = view.findViewById(R.id.sendImageBtn);
        this.inputMessageEtv = view.findViewById(R.id.inputMsgetv);
    }

    private void initRecycler(View view) {
        this.recyclerView = view.findViewById(R.id.chatRecyclerView);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        this.recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        this.mLayoutManager = new LinearLayoutManager(getActivity());
        this.recyclerView.setLayoutManager(mLayoutManager);
        adapter = new ChatAdapter(groupController, currentMessageList);
        recyclerView.setAdapter(adapter);
    }

    public void setGroupController(GroupController groupController){
        this.groupController = groupController;
    }

    public void updateList(ArrayList<TextMessage> messageList) {
        currentMessageList.clear();
        currentMessageList.addAll(messageList);
        adapter.notifyDataSetChanged();
    }

}
