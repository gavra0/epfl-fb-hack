package com.hackers.epfl.asynctasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.hackers.epfl.Constants;
import com.hackers.epfl.R;

/**
 * @author Ivan Gavrilovic
 */
public class UploadMediaAsyncTask extends AsyncTask<Void, Void, Void> {
	public static String TAG = UploadMediaAsyncTask.class.getCanonicalName();

	private final Context context;
	private final String beaconID;
	private final String path;
	private final String type;

	private MultiValueMap<String, Object> formData;

	public UploadMediaAsyncTask(Context context, String beaconID, String path, String type) {
		this.context = context;
		this.beaconID = beaconID;
		this.path = path;
		this.type = type;

        try {
            // populate the data to post
            formData = new LinkedMultiValueMap<String, Object>();
            formData.add(Constants.PARAM_BEACON_ID, beaconID);

            byte[] bytes = read(new File(path));
            byte[] encoded = Base64.encode(bytes, Base64.DEFAULT);
            String encodedString = new String(encoded);
            formData.add("file", encodedString);
        }
        catch (Exception e){
            //IO Exception
            e.printStackTrace();
        }
	}

	@Override
	protected Void doInBackground(Void... params) {
		Log.d(TAG, "Uploading file");

		try {
			final String url = context.getResources().getString(R.string.gnote_url);

			HttpHeaders requestHeaders = new HttpHeaders();

			// Sending multipart/form-data
			requestHeaders.setContentType(MediaType.APPLICATION_JSON);

			// Populate the MultiValueMap being serialized and headers in an HttpEntity object to
			// use for the request
			HttpEntity<MultiValueMap<String, Object>> requestEntity =
					new HttpEntity<MultiValueMap<String, Object>>(formData, requestHeaders);

			// Create a new RestTemplate instance
			RestTemplate restTemplate = new RestTemplate(true);
            restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());

			restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

			// Return the response body to display to the user
			return null;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}

		return null;
	}

	public byte[] read(File file) throws IOException {
		byte[] buffer = new byte[(int) file.length()];
		InputStream ios = null;
		try {
			ios = new FileInputStream(file);
			if (ios.read(buffer) == -1) {
				throw new IOException("EOF reached while trying to read the whole file");
			}
		} finally {
			try {
				if (ios != null)
					ios.close();
			} catch (IOException e) {
			}
		}

		return buffer;
	}
}
