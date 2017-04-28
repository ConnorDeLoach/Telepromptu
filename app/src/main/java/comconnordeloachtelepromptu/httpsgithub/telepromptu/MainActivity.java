package comconnordeloachtelepromptu.httpsgithub.telepromptu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;

import java.util.List;

import comconnordeloachtelepromptu.httpsgithub.telepromptu.drive.DownloadDocs;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String ACTION_DRIVEID_RECEIVED = "comconnordeloachtelepromptu.httpsgithub.telepromptu.drive.id.received";
    private static final String TAG = "MainActivity";
    final ResultCallback<DriveFolder.DriveFolderResult> callback = new ResultCallback<DriveFolder.DriveFolderResult>() {
        @Override
        public void onResult(DriveFolder.DriveFolderResult result) {
            if (!result.getStatus().isSuccess()) {
                Toast.makeText(MainActivity.this, "Error while trying to create the folder", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(MainActivity.this, "Created a folder: " + result.getDriveFolder().getDriveId(), Toast.LENGTH_SHORT).show();
        }
    };
    private GoogleApiClient mGoogleApiClient;
    private BroadcastReceiver mDriveRestCallBack;
    private List<String> mDriveIdList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register Drive REST callback
        mDriveRestCallBack = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mDriveIdList = intent.getStringArrayListExtra("driveid");

            }
        };
        IntentFilter filter = new IntentFilter(ACTION_DRIVEID_RECEIVED);
        this.registerReceiver(mDriveRestCallBack, filter);

        Intent intent = new Intent(this, DownloadDocs.class);
        this.startActivity(intent);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addScope(Drive.SCOPE_APPFOLDER)
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Connection to DriveAPI
        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                .setTitle("Speeches")
                .build();
        Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(mGoogleApiClient, changeSet).setResultCallback(callback);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and
        // an authorization dialog is displayed to the user.
        try {
            result.startResolutionForResult(this, ConnectionResult.SERVICE_DISABLED);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
            Toast.makeText(this, R.string.drive_api_resolution_failure, Toast.LENGTH_SHORT).show();
        }
    }
}