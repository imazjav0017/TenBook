package com.mansa.StaySpace.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mansa.StaySpace.LoginActivity;
import com.mansa.StaySpace.Owner.UpdateOwnerExtraActivity;
import com.mansa.StaySpace.Owner.UpdateOwnerProfileActivity;
import com.mansa.StaySpace.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by nitish on 27-03-2018.
 */

public class OwnerProfileFragment extends Fragment {
    TextView name,email,phNo;
    Context context;
    CircleImageView img;
    CardView updateProfile;
    Button editProfile;
    public OwnerProfileFragment() {
    }

    public OwnerProfileFragment(Context context) {
        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        setDetails();
        try {
            JSONArray buildings=new JSONArray(LoginActivity.sharedPreferences.getString("buildings",null));
            if(buildings.length()==0)
                    updateProfile.setVisibility(View.VISIBLE);
                else
                    updateProfile.setVisibility(View.INVISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.owner_profile_fragment,container,false);
        name=(TextView) v.findViewById(R.id.ownerNameTabText);
        email=(TextView) v.findViewById(R.id.ownerEmailText);
        phNo=(TextView) v.findViewById(R.id.ownerNumbertext);
        editProfile=(Button)v.findViewById(R.id.editOwnerProfileButton);
        updateProfile=(CardView) v.findViewById(R.id.updateOwnerDetailsExtraBg);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editProfileAction();
            }
        });
        img=v.findViewById(R.id.img);

        //if no building only then set as visible


        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfileAction();
            }
        });
       /* img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PhotoActivity.class));
            }
        });*/

        return v;
    }
    public void updateProfileAction()
    {
        Intent i=new Intent(context, UpdateOwnerExtraActivity.class);
        startActivity(i);
    }

    public void editProfileAction()
    {
        Log.i("editProfileBtn","1");
        Intent i=new Intent(context, UpdateOwnerProfileActivity.class);
        startActivity(i);
    }
    //set the profile info
    void setDetails()
    {
        String s= LoginActivity.sharedPreferences.getString("ownerDetails",null);
        try {
            JSONObject object=new JSONObject(s);
            JSONObject ownerNameObject=object.getJSONObject("name");
            String ownerName=ownerNameObject.getString("firstName")+" "+ownerNameObject.getString("lastName");
            name.setText(ownerName);
            email.setText(object.getString("email"));
            phNo.setText(object.getString("mobileNo"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
