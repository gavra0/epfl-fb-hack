package com.hackers.epfl.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.hackers.epfl.BeaconAPIMessage;
import com.hackers.epfl.Constants;
import com.hackers.epfl.R;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

/**
 * @author Filip Hrisafov
 */
public class SendMessageAsyncTask extends AsyncTask<Void, Void, Void> {
	public static String TAG = SendMessageAsyncTask.class.getCanonicalName();

	private final Context context;
	private final String json;

	public SendMessageAsyncTask(Context context, String beaconID, String message) {
		this.context = context;
        this.json = "request={\""+Constants.PARAM_BEACON_ID+"\":"+beaconID+", \""+Constants.PARAM_MESSAGE+"\":"+message+"}";
	}

	@Override
	protected Void doInBackground(Void... params) {
        Log.d(TAG, "Sending msg");
		BeaconAPIMessage.BeaconRequestResponse result = null;

		final String url = context.getResources().getString(R.string.gnote_url);

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        requestHeaders.setContentType(MediaType.APPLICATION_JSON);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
		body.add(Constants.PARAM_REQUEST, json);

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());
		restTemplate.getMessageConverters().add(new FormHttpMessageConverter());

		ResponseEntity<BeaconAPIMessage> response = null;
		try {
			response =
					restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<Object>(body,
							requestHeaders), BeaconAPIMessage.class);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}

		if (response != null) {
			BeaconAPIMessage beaconAPIMessage = response.getBody();
			if (beaconAPIMessage != null && beaconAPIMessage.response != null) {
				result = beaconAPIMessage.response;
			}
			// TODO
		}
		return null;
	}
}
