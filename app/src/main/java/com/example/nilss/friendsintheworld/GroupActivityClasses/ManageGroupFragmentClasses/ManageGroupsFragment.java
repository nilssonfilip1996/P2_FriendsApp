package com.example.nilss.friendsintheworld.GroupActivityClasses.ManageGroupFragmentClasses;


import android.app.AlertDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.nilss.friendsintheworld.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class ManageGroupsFragment extends Fragment {
    private static final String TAG = "ManageGroupsFragment";
    private FloatingActionButton floatingActionButtonAddGrp;

    public ManageGroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_manage_groups, container, false);
        Log.d(TAG, "onCreateView: testing testing...");
        initComponents(view);
        return view;
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
                //dialog.hide();
                dialog.dismiss();
            });
            dialog.show();
        });
    }

}
