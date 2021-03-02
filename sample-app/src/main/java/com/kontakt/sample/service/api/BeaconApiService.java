package com.kontakt.sample.service.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.kontakt.sample.utils.config;
import com.kontakt.sample.service.Interfaces.BeaconResponseListener;
import com.kontakt.sample.service.MyJsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BeaconApiService {

    BeaconResponseListener mListener;

    public void connect(Context context, String sim, String uuid, int major, int minor, String latitud, String longitud, String dato1,
                        String dato2, String dato3, BeaconResponseListener mlistener) {
        this.mListener = mlistener;
        String responseString = "";
        mListener.requestStarted();
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("SIM", sim);
            jsonBody.put("DeviceId", uuid);
            jsonBody.put("Major", major);
            jsonBody.put("Minor", minor);
            jsonBody.put("Latitud", latitud);
            jsonBody.put("Longitud", longitud);
            jsonBody.put("Dato1", dato1);
            jsonBody.put("Dato2", dato2);
            jsonBody.put("Dato3", dato3);
            Log.i("LOG_RESPONSE", "test");

            MyJsonArrayRequest stringRequest = new MyJsonArrayRequest(Request.Method.POST, config.BASE_URL + config.REGISTRO_BEACON_URL, jsonBody, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Log.i("LOG_RESPONSE", response.toString());

                    JSONObject resp =null;
                    try {

                        if(response !=null){
                            for(int i=0; i<response.length(); i++){
                                resp = response.getJSONObject(i);
                            }
                            System.out.println(resp.toString());
                        }
                        mListener.requestCompleted();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("LOG_RESPONSE", error.toString());
                    mListener.requestEndedWithError(error);
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    return params;
                }
            };
            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }
}
