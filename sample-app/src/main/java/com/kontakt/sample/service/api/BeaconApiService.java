package com.kontakt.sample.service.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.kontakt.sample.Utils.config;
import com.kontakt.sample.service.Interfaces.BeaconResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class BeaconApiService {

    BeaconResponseListener mListener;

    public void connect(Context context, String sim, String uuid, String latitud, String longitud, BeaconResponseListener mlistener) {
        this.mListener = mlistener;
        String responseString = "";
        mListener.requestStarted();
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("SIM", sim);
            jsonBody.put("DeviceId", uuid);
            jsonBody.put("Latitud", latitud);
            jsonBody.put("Longitud", longitud);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, config.BASE_URL + config.REGISTRO_BEACON_URL, jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i("LOG_RESPONSE", response.toString());
                    try {
                        if (response != null) {
                            Log.i("LOG_RESPONSE", response.get("Response").toString());
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
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }
}
