package dte.masteriot.mdp.asteroidconspiracist.util.location;

import com.google.android.gms.maps.model.LatLng;


//Interface  to handle the location callback after it is retrieved.
//AG

public interface LocationCallback
{
    void onLocationRetrieved(LatLng latLng);
}