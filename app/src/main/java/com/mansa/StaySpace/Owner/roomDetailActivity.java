package com.mansa.StaySpace.Owner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.mansa.StaySpace.Adapters.PaymentHistoryAdapter;
import com.mansa.StaySpace.Adapters.StudentAdapter;
import com.mansa.StaySpace.AsyncTasks.CheckoutTask;
import com.mansa.StaySpace.AsyncTasks.PaymentTask;
import com.mansa.StaySpace.DataModels.PaymentHistoryModel;
import com.mansa.StaySpace.DataModels.StudentModel;
import com.mansa.StaySpace.LoginActivity;
import com.mansa.StaySpace.R;
import com.mansa.StaySpace.Tenants.SendRequestActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class roomDetailActivity extends AppCompatActivity {
    TextView rn,rt,rr,studentsExpandingView,paymentsExpandLayout,dueAmount,roomCapacity,totalRoomCapacity
           , checkInDateTv,dueDateTv,checkInDateTitle;
    RecyclerView studentsRV,paymentsHistoryList;
    StudentAdapter adapter;
    List<StudentModel> studentsList;
    List<PaymentHistoryModel>paymentList;
    Button checkOut;
    PaymentHistoryAdapter pAdapter;
    ExpandableRelativeLayout expandableRelativeLayout,expandablePayments;
    String roomNo,roomType,roomRent,_id,response,dueAmnt,roomC,totalRoomC,checkInDate,checkOutDate,dueDate;
    boolean fromTotal,fromEditOrDelete,isEmpty;
    ProgressDialog progressDialog;

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        this.finish();
    }

    public void setTokenJson(String mode)
    {    DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if(LoginActivity.sharedPreferences.getString("token",null)!=null) {
                JSONObject token = new JSONObject();
                token.put("auth",LoginActivity.sharedPreferences.getString("token", null));
                token.put("roomId",_id);
                token.put("date" ,dateFormat.format(new Date()).toString());
                if(mode.equals("delete")) {
                    DeleteRoomsTask task = new DeleteRoomsTask();
                    task.execute(LoginActivity.MAINURL+"/rooms/deleteRooms", token.toString());
                    progressDialog=new ProgressDialog(roomDetailActivity.this);
                    progressDialog.setTitle("Deleting Room");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.setMax(100);
                    progressDialog.setMessage("Deleting "+roomNo);
                    progressDialog.show();
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String  getResponse(HttpURLConnection connection)
    {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
            StringBuffer sb = new StringBuffer("");
            String line = "";

            while ((line = in.readLine()) != null) {

                sb.append(line);
                break;
            }

            in.close();
            return sb.toString();
        }catch(Exception e)
        {
            return e.getMessage();
        }
    }
    public class DeleteRoomsTask extends AsyncTask<String,Void,String>
    {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.addRequestProperty("Accept", "application/json");
                connection.addRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.connect();
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(params[1]);
                Log.i("data", params[1]);
                int resp = connection.getResponseCode();
                Log.i("deleteRoomResp",String.valueOf(resp));
                /*if(resp==200)
                {
                    response=getResponse(connection);
                    return response;
                }
                else
                {
                    return null;
                }*/
                response=getResponse(connection);
                return response;

            }catch(MalformedURLException e)
            {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if (s != null) {
                Log.i("deleteRoomResp",s);
                try {
                    JSONObject response=new JSONObject(s);
                    int code=response.getInt("code");
                    if(code==200)
                    {
                        Toast.makeText(roomDetailActivity.this, "Room Deleted!", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                    else
                    {
                        String message=response.getString("message");
                        Toast.makeText(roomDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(roomDetailActivity.this, "Some Error Occured", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(roomDetailActivity.this, "Please Check Your Internet Connection and try later!", Toast.LENGTH_SHORT).show();
                super.onPostExecute(s);
            }
        }
    }
    public void deleteRoom()
    {
        new AlertDialog.Builder(this)
                .setTitle("Delete!").setMessage("Are You Sure You Wish To Delete Room No "+roomNo)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setTokenJson("delete");
                    }
                })
               .setNegativeButton("No",null).show();
    }
    public void checkOut(View v)
    {
        if(checkOut.getText().toString().equals("Vacate")) {
            new AlertDialog.Builder(this)
                    .setTitle("Vacate!").setMessage("Are You Sure You Wish To Vacate All Tenants From Room No " + roomNo + "?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                                if (dueAmnt != null) {
                                    Log.i("vacate",""+Integer.parseInt(dueAmnt));
                                    if (Integer.parseInt(dueAmnt) != 0) {
                                        new AlertDialog.Builder(roomDetailActivity.this).setTitle("Unpaid Rent").setMessage("Rent is still due for this room ,are you sure you wish to vacate this room?")
                                                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        try {
                                                            startCheckout();
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }).setNegativeButton("no",null).show();
                                    }
                                    else
                                    {
                                        try {
                                            startCheckout();
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                        }
                    })
                    .setNegativeButton("No", null).show();
        }
        else
        {
            Intent i=new Intent(getApplicationContext(),StudentActivity.class);
            i.putExtra("id",_id);
            i.putExtra("roomNo",roomNo);
            startActivity(i);
            finish();
        }

    }
    void startCheckout() throws JSONException {
            JSONObject data = new JSONObject();
            String auth = LoginActivity.sharedPreferences.getString("token", null);
            data.put("auth", auth);
            data.put("roomId", _id);
            CheckoutTask task = new CheckoutTask();
            task.execute(LoginActivity.MAINURL+"/rooms/vacateRooms", data.toString());
        progressDialog=new ProgressDialog(roomDetailActivity.this);
        progressDialog.setTitle("Vacating Room");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(100);
        progressDialog.setMessage("Vacating.... "+roomNo);
        progressDialog.show();

    }
    class CheckoutTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.addRequestProperty("Accept", "application/json");
                connection.addRequestProperty("Content-Type", "application/json");
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.connect();
                DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(params[1]);
                Log.i("VACATEDATA", params[1]);
                int resp = connection.getResponseCode();
                Log.i("VACATERESP", String.valueOf(resp));
                if (resp == 422) {
                    return "First clear Dues!";
                } else if (resp == 200) {
                    return "checked out from Room";
                } else {
                    return null;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialog.dismiss();
            if (s != null) {
                Toast.makeText(roomDetailActivity.this, s, Toast.LENGTH_SHORT).show();
                if (s.equals("checked out from Room")) {
                    onBackPressed();
                }
                else if (s.equals("First clear Dues!")) {
                    Toast.makeText(roomDetailActivity.this, s, Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(roomDetailActivity.this, "Please Check Your Internet Connection and try later!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void editRoom()
    {
        Intent i=new Intent(getApplicationContext(),edit_rooms.class);
        i.putExtra("roomNo",roomNo);
        i.putExtra("roomRent",roomRent);
        i.putExtra("roomType",roomType);
        i.putExtra("id",_id);
        i.putExtra("fromTotal",fromTotal);
        startActivity(i);
        finish();
    }
    public  void addStudent(View v)
    {
        Intent i=new Intent(getApplicationContext(),StudentActivity.class);
        i.putExtra("id",_id);
        i.putExtra("roomNo",roomNo);
        i.putExtra("fromDetails",true);
        startActivity(i);
        finish();
    }
    public void setPaymentHistory(String s) throws JSONException {
        paymentList.clear();
        if(s!=null)
        {
            JSONArray mainArray=new JSONArray(s);
            for(int i=0;i<mainArray.length();i++) {
                JSONObject mainObject=mainArray.getJSONObject(i);
                if(_id.equals(mainObject.getString("_id"))) {
                    JSONArray paymentArray = mainObject.getJSONArray("paymentDetail");
                    for(int j=0;j<paymentArray.length();j++) {
                        JSONObject paymentObject=paymentArray.getJSONObject(j);
                        boolean payStatus=paymentObject.getBoolean("payStatus");
                        String date=paymentObject.getString("date");
                        if(payStatus) {
                            String payee = paymentObject.getString("payee");
                            String amount = String.valueOf(paymentObject.getInt("amount"));
                            paymentList.add(new PaymentHistoryModel(payee,amount,date,payStatus));
                        }
                        else
                        {
                            String reason=paymentObject.getString("reason");
                            paymentList.add(new PaymentHistoryModel(reason,date,payStatus));

                        }

                    }


                }
            }
        }
        else
            Log.i("s","is null");
        pAdapter.notifyDataSetChanged();

    }
    void setStudentsData(String s) throws JSONException {

        if (s != null) {
            studentsList.clear();
            JSONObject mainObject = new JSONObject(s);
            JSONArray tenantsArray = mainObject.getJSONArray("tenants");
            JSONArray studentsArray = mainObject.getJSONArray("student");
            for (int i = 0; i < studentsArray.length(); i++) {
                JSONObject roomDetail = studentsArray.getJSONObject(i);
                String roomId = roomDetail.getString("_id");
                if(roomId.equals(_id)) {
                    String roomNo = roomDetail.getString("roomNo");
                    JSONArray studentsDetailArray = roomDetail.getJSONArray("students");
                    for (int j = 0; j < studentsDetailArray.length(); j++) {
                        JSONObject studentDetails = studentsDetailArray.getJSONObject(j);
                        String studentId = studentDetails.getString("_id");
                        String name = studentDetails.getString("name");
                        String mobileNo = studentDetails.getString("mobileNo");
                        String adharNo = studentDetails.getString("adharNo");
                        studentsList.add(new StudentModel(name, mobileNo, roomNo, studentId, adharNo, roomId,false));
                    }
                }
                else
                    continue;
            }
            for (int i = 0; i < tenantsArray.length(); i++) {
                JSONObject tenantObject = tenantsArray.getJSONObject(i);
                String tenantId = tenantObject.getString("_id");
                String mobileNo = tenantObject.getString("mobileNo");
                JSONObject nameObject = tenantObject.getJSONObject("name");
                JSONArray idProofs=tenantObject.getJSONArray("idProofPics");
                List<String> idProofPics=new ArrayList<>();
                if(idProofs.length()>0) {
                    for (int k = 0; k < idProofs.length(); k++) {
                        idProofPics.add(idProofs.getString(k));
                    }
                }
                String name = nameObject.getString("firstName") + " " + nameObject.getString("lastName");
                JSONObject roomObject = tenantObject.getJSONObject("room");
                String roomId = roomObject.getString("_id");
                String roomNo = roomObject.getString("roomNo");
                String adharNo = tenantObject.getString("adharNo");
                if(roomId.equals(_id))
                studentsList.add(new StudentModel(name, mobileNo, roomNo, tenantId, adharNo, roomId,true,idProofPics));
            }
           /* if (studentsList.size() == 0) {
                checkOut.setText("Checkin");
            }*/
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            setStudentsData(LoginActivity.sharedPreferences.getString("allTenants",null));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            setPaymentHistory(LoginActivity.sharedPreferences.getString("getRoomsResp",null));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.room_detail_options,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.deleteRoomOption:
                deleteRoom();
                return true;
            case R.id.editRoomOption:
                editRoom();
                return true;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.owner_activity_room_detail);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fromEditOrDelete=false;
        Intent i=getIntent();
        isEmpty=i.getBooleanExtra("isEmpty",true);
        _id=i.getStringExtra("id");
        roomNo=i.getStringExtra("roomNo");
        roomType=i.getStringExtra("roomType");
        fromTotal=i.getBooleanExtra("fromTotal",false);
        roomRent=i.getStringExtra("roomRent");
        dueAmnt=i.getStringExtra("due");
        roomC=String.valueOf(i.getIntExtra("roomCapacity",0));
        totalRoomC=String.valueOf(i.getIntExtra("totalRoomCapacity",0));
        setTitle("RoomNo: "+i.getStringExtra("roomNo"));
        checkOut=(Button)findViewById(R.id.empt_checkin);
        rn = (TextView) findViewById(R.id.roomno);
        rt = (TextView) findViewById(R.id.roomtype);
        rr = (TextView) findViewById(R.id.roomrent);
        checkInDateTitle=(TextView)findViewById(R.id.checkInDateTitleTv);
        checkInDateTv=(TextView)findViewById(R.id.checkInDateText);
        dueDateTv=(TextView)findViewById(R.id.dueDateText);
        dueAmount=(TextView)findViewById(R.id.dueAmount);
        roomCapacity=(TextView)findViewById(R.id.roomCapacity);
        totalRoomCapacity=(TextView)findViewById(R.id.totalRoomCapacity);
        studentsExpandingView = (TextView) findViewById(R.id.studentsExpandingView);
        paymentsExpandLayout = (TextView) findViewById(R.id.paymentsExpandLayout);
        expandableRelativeLayout=(ExpandableRelativeLayout)findViewById(R.id.studentsLayout);
        expandablePayments=(ExpandableRelativeLayout)findViewById(R.id.paymentsLayout);



        if(isEmpty) {
            checkOut.setText("Checkin");
            checkInDateTitle.setText("CheckOut Date");
            checkOutDate=i.getStringExtra("checkOutDate");
            if(checkOutDate!=null)
            if(!checkOutDate.equals("0"))
            {
                checkInDateTv.setText(checkOutDate);
            }
        }
        else
        {
            checkInDate=i.getStringExtra("checkInDate");
            dueDate=i.getStringExtra("dueDate");
            if(dueDate!=null)
                dueDateTv.setText(dueDate);
            if(checkInDate!=null)
                checkInDateTv.setText(checkInDate);

        }
        rn.setText(roomNo);
        rt.setText(roomType);
        rr.setText("\u20B9"+roomRent);
        if(dueAmnt!=null)
            dueAmount.setText("\u20B9"+dueAmnt);
        roomCapacity.setText(roomC);
        totalRoomCapacity.setText(totalRoomC);
        studentsRV=(RecyclerView)findViewById(R.id.studentsRecyclerView);
        studentsList=new ArrayList<>();
        adapter=new StudentAdapter(studentsList,getApplicationContext());
        LinearLayoutManager lm=new LinearLayoutManager(getApplicationContext());
        studentsRV.setLayoutManager(lm);
        studentsRV.setHasFixedSize(true);
        studentsRV.setAdapter(adapter);
        paymentList=new ArrayList<>();
        paymentsHistoryList=(RecyclerView)findViewById(R.id.paymentsHistoryList);
        pAdapter=new PaymentHistoryAdapter(paymentList);
        LinearLayoutManager lm2=new LinearLayoutManager(getApplicationContext());
        paymentsHistoryList.setLayoutManager(lm2);
        paymentsHistoryList.setHasFixedSize(true);
        paymentsHistoryList.setAdapter(pAdapter);
        if(fromEditOrDelete)
        {

        }

    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void expandStudents(View v)
    {
        if(expandableRelativeLayout.isExpanded())
        {
            studentsExpandingView.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_down_arrow,0);
            expandableRelativeLayout.collapse();
        }
        else {
            studentsExpandingView.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_up_arrow,0);
            expandableRelativeLayout.toggle();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void expandPayments(View v)
    {
        if(expandablePayments.isExpanded())
        {
            paymentsExpandLayout.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_down_arrow,0);
            expandablePayments.collapse();
        }
        else {
            paymentsExpandLayout.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.ic_up_arrow,0);
            expandablePayments.toggle();
        }
    }


}
