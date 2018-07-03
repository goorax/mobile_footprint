package de.tu_berlin.mobilefootprint.util;


import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MozillaLocationService {

    @POST("/v1/geolocate?key=test")
    Call<JsonObject> getLocation(@Body JsonObject request);
}
