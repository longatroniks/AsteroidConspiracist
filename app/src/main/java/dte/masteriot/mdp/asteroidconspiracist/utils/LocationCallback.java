package dte.masteriot.mdp.asteroidconspiracist.utils;

import com.google.android.gms.maps.model.LatLng;


//Interface  to handle the location callback after it is retrieved.
//AG

public interface LocationCallback
{
    void onLocationRetrieved(LatLng latLng);
}