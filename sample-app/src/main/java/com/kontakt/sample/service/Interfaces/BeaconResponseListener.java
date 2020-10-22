package com.kontakt.sample.service.Interfaces;

import com.android.volley.VolleyError;

public interface BeaconResponseListener {
    void requestStarted();
    void requestCompleted();
    void requestEndedWithError(VolleyError error);
}
