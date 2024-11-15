package dte.masteriot.mdp.asteroidconspiracist.util.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkHelper {

    private final Context context;
    private BroadcastReceiver networkReceiver;

    public NetworkHelper(Context context) {
        this.context = context;
    }

    /**
     * Checks if the network is available.
     *
     * @return true if the network is available, false otherwise.
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * Registers a network receiver to listen for connectivity changes.
     *
     * @param onNetworkAvailable Callback to handle network availability.
     */
    public void registerNetworkReceiver(Runnable onNetworkAvailable) {
        networkReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isNetworkAvailable()) {
                    onNetworkAvailable.run();
                }
            }
        };
        context.registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    /**
     * Unregisters the network receiver.
     */
    public void unregisterNetworkReceiver() {
        if (networkReceiver != null) {
            context.unregisterReceiver(networkReceiver);
            networkReceiver = null;
        }
    }
}
