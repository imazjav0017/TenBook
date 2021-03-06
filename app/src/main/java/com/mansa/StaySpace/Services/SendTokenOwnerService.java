package com.mansa.StaySpace.Services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mansa.StaySpace.LoginActivity;

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

public class SendTokenOwnerService extends IntentService {
    public SendTokenOwnerService() {
        super("SendTokenOwnerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            try {
                startTask();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    void startTask() throws JSONException {
        JSONObject data=new JSONObject();
        String auth= LoginActivity.sharedPreferences.getString("token",null);
        String nToken=LoginActivity.sharedPreferences.getString("nToken",null);
        if(auth!=null && nToken!=null)
        {
            data.put("auth",auth);
            data.put("nToken",nToken);
            SendTask task=new SendTask();
            task.execute(LoginActivity.MAINURL+"/users/getTokenOwner",data.toString());
        }
        else
        {
            Log.i("Token","unable to send");
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
    private class SendTask extends AsyncTask<String,Void,String>
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
                Log.i("SENDOWNERTOKENDATA", params[1]);
                int resp = connection.getResponseCode();
                Log.i("SENDOWNERTOKENRESP",String.valueOf(resp));
                if(resp==200)
                {
                    String response=getResponse(connection);
                    return response;
                }
                else
                {
                    return null;
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
            if (s != null) {
               Log.i("SENDOWNERTOKENRESP",s);
            }
            else
            {
                Log.i("SENDOWNERTOKENRESP","ERRR");
                super.onPostExecute(s);
            }
        }
    }
}
