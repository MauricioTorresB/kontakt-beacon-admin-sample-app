package com.kontakt.sample.service.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.kontakt.sample.utils.config;
import com.kontakt.sample.service.Interfaces.TestUrlResponseListener;
import com.kontakt.sample.service.MyJsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TestUrlApiService {

    TestUrlResponseListener mListener;

    public void test_url(Context context, String url, TestUrlResponseListener mlistener) {
        this.mListener = mlistener;
        mListener.requestStarted();

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject jsonBody = new JSONObject();

        //jsonBody.put(TAG_USUARIO, datosUsuario.getUsername());

        MyJsonArrayRequest stringRequest = new MyJsonArrayRequest(Request.Method.POST, url + config.TEST_URL, jsonBody, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.i("LOG_RESPONSE", response.toString());

                JSONObject jsbulto = null;
                mListener.requestCompleted();
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
    }
}
