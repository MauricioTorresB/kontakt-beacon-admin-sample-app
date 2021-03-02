package com.kontakt.sample.service.Interfaces;

import com.android.volley.VolleyError;

public interface TestUrlResponseListener {
    void requestStarted();
    void requestCompleted();
    void requestEndedWithError(VolleyError error);
}
