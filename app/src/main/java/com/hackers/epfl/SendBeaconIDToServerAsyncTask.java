package com.hackers.epfl;

import java.util.Collections;

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

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @author Filip Hrisafov
 */
public class SendBeaconIDToServerAsyncTask extends
		AsyncTask<Void, Void, BeaconAPIMessage.BeaconRequestResponse> {

	public static final String PARAM_BEACON_ID = "beaconID";

	public static String TAG = SendBeaconIDToServerAsyncTask.class.getCanonicalName();

	private final Context context;
	private final String beaconID;
	private final ISendBeaconIDToServerResultHandler handler;

	public SendBeaconIDToServerAsyncTask(Context context, String beaconID,
			ISendBeaconIDToServerResultHandler handler) {
		this.context = context;
		this.beaconID = beaconID;
		this.handler = handler;
	}

	@Override
	protected BeaconAPIMessage.BeaconRequestResponse doInBackground(Void... params) {

		BeaconAPIMessage.BeaconRequestResponse result = null;

		final String url =
				context.getResources().getString(R.string.request_message_for_beacon_url, beaconID);

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		requestHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
		body.add(PARAM_BEACON_ID, beaconID);

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
		return result;
	}

	@Override
	protected void onPostExecute(BeaconAPIMessage.BeaconRequestResponse responseResult) {
		super.onPostExecute(responseResult);
		handler.onPostExecute(responseResult);
	}

	public interface ISendBeaconIDToServerResultHandler {
		public void onPostExecute(BeaconAPIMessage.BeaconRequestResponse responseResult);

	}
}
