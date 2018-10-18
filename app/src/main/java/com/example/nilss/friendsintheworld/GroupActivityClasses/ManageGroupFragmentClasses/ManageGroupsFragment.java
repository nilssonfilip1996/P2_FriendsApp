package com.example.nilss.friendsintheworld.GroupActivityClasses.ManageGroupFragmentClasses;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.nilss.friendsintheworld.GroupActivityClasses.GroupController;
import com.example.nilss.friendsintheworld.OnCreateViewLIstener;
import com.example.nilss.friendsintheworld.R;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ManageGroupsFragment extends Fragment {
    private static final String TAG = "ManageGroupsFragment";
    private GroupController groupController;
    private ArrayList<String> currentGroupsList;    //groupnames
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private ManageGroupsAdapter adapter;
    private FloatingActionButton floatingActionButtonAddGrp;
    private OnCreateViewLIstener mListener;

    public void onInit(OnCreateViewLIstener listener){
        this.mListener = listener;
    }

    public ManageGroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_manage_groups, container, false);
        currentGroupsList = new ArrayList<>();
        initRecycler(view);
        initComponents(view);
        if(mListener != null){
            mListener.onViewsInitialized();
        }
        return view;
    }

    private void initRecycler(View view) {
        this.recyclerView = view.findViewById(R.id.groupsRecyclerView);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        this.recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        this.mLayoutManager = new LinearLayoutManager(getActivity());
        this.recyclerView.setLayoutManager(mLayoutManager);
        // specify an adapter. MainAdapter is a selfwritten class.
        adapter = new ManageGroupsAdapter(groupController, currentGroupsList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClicked(int position) {
                //group in list clicked!
                Log.d(TAG, "onItemClicked: " + currentGroupsList.get(position));
                groupController.groupInListClicked(currentGroupsList.get(position), true);
            }

            @Override
            public void onItemLongClicked(int position) {
                Log.d(TAG, "onItemClicked: " + currentGroupsList.get(position));
                groupController.groupInListClicked(currentGroupsList.get(position), false);
            }
        });
    }

    private void initComponents(View view) {
        Log.d(TAG, "initComponents: initialization");
        floatingActionButtonAddGrp = view.findViewById(R.id.floatingActionButtonAddGroup);
        floatingActionButtonAddGrp.setOnClickListener((View v)->{
            Log.d(TAG, "initComponents: float pressed!");
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
            View mView = getLayoutInflater().inflate(R.layout.dialog_addgroup, null);
            EditText groupNameEtv = (EditText) mView.findViewById(R.id.groupNameetv);
            Button commitBtn = (Button) mView.findViewById(R.id.commitGroupBtn);
            mBuilder.setView(mView);
            AlertDialog dialog = mBuilder.create();
            commitBtn.setOnClickListener((View v2)->{
                //sent to controller for validation.
                Log.d(TAG, "initComponents: Groupname: " + groupNameEtv.getText().toString());
                groupController.addGroup(groupNameEtv.getText().toString());
                dialog.dismiss();
            });
            dialog.show();
        });
    }

    public void setGroupController(GroupController groupController){
        this.groupController = groupController;
    }

    public void updateList(ArrayList<String> groupList) {
        currentGroupsList.clear();
        currentGroupsList.addAll(groupList);
        adapter.notifyDataSetChanged();
    }

}
