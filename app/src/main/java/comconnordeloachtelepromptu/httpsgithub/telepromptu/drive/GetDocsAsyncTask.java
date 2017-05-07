package comconnordeloachtelepromptu.httpsgithub.telepromptu.drive;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An asynchronous task that handles the Drive API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
public class GetDocsAsyncTask extends AsyncTask<Void, Void, List<String>> {
    private com.google.api.services.drive.Drive mService = null;
    private Context mContext;

    public GetDocsAsyncTask(Context context, GoogleAccountCredential credential) {
        mContext = context;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, credential)
                .setApplicationName("Telepromptu")
                .build();
    }

    /**
     * Background task to call Drive API.
     *
     * @param params no parameters needed for this task.
     */
    @Override
    protected List<String> doInBackground(Void... params) {
        try {
            return getDataFromApi();
        } catch (Exception e) {
            cancel(true);
            return null;
        }
    }

    /**
     * Fetch a list of up all file Ids with mime-type doc.
     *
     * @return List of Strings describing files, or an empty list if no files
     * found.
     * @throws IOException is thrown
     */
    private List<String> getDataFromApi() throws IOException {
        // Get all documents on drive
        List<String> fileInfo = new ArrayList<String>();
        FileList result = mService.files().list()
                .setQ("mimeType = 'application/vnd.google-apps.document'")
                .setFields("files(id)")
                .execute();
        List<File> files = result.getFiles();
        if (files != null) {
            for (File file : files) {
                fileInfo.add(String.format("%s",
                        file.getId()));
            }
        }
        return fileInfo;
    }

    @Override
    protected void onPostExecute(List<String> output) {
        if (output == null || output.size() == 0) {
            Log.e("GetDocsAsyncTask", "onPostExecute no results returned");
        } else {
            Intent intent = new Intent();
            intent.putStringArrayListExtra("driveid", (ArrayList<String>) output);
            mContext.sendBroadcast(intent);
        }
    }
}
