package com.mansa.StaySpace.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mansa.StaySpace.LoginActivity;
import com.mansa.StaySpace.Services.GetBuilidngsService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class AddBuildingTask extends AsyncTask<String,Void,String> {
    Context context;
    int mode; //0-add , 1-edit
    public interface AddBuildingResp
    {
        void processFinish(Boolean output);
    }

    AddBuildingResp addBuildingResp=null;
    public AddBuildingTask(Context context,AddBuildingResp resp,int mode) {
        this.context = context;
        this.addBuildingResp=resp;
        this.mode=mode;
    }
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
            Log.i("data_AddBuildingTask", params[1]);
            int resp = connection.getResponseCode();
            Log.i("AddBuildingResp",String.valueOf(resp));
            if(resp==200)
            {
                String response=getResponse(connection);
                return response;
            }
            else if(resp==422)
            {
                return "422";
            }

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
        super.onPostExecute(s);
       if(s!=null)
       {
           if(s.equals("422"))
           {
               Toast.makeText(context, "Building With Same Name Already Exists", Toast.LENGTH_SHORT).show();
           }
           else {
               Log.i("addBuildingResp", s);
               if (mode == 0) {
                   JSONObject mainObject = null;
                   try {
                       mainObject = new JSONObject(s);
                       JSONArray buildings = mainObject.getJSONArray("build");
                       LoginActivity.sharedPreferences.edit()
                               .putString("buildings", buildings.toString()).apply();
                   } catch (JSONException e) {
                       e.printStackTrace();
                   }

                   addBuildingResp.processFinish(true);
                   Toast.makeText(context, "Building Added!", Toast.LENGTH_SHORT).show();
               }
               else if(mode==1)
               {
                   addBuildingResp.processFinish(true);
                   Toast.makeText(context, "Building Saved!", Toast.LENGTH_SHORT).show();
               }
           }

       }
        else
        {
            Toast.makeText(context, "Please Check Your Internet Connection and try later!", Toast.LENGTH_SHORT).show();
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
}
