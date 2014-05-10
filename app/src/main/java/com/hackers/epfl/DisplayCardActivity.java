package com.hackers.epfl;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.google.android.glass.app.Card;

/**
 * @author Filip Hrisafov
 */
public class DisplayCardActivity extends Activity {

	public static String CARD_TEXT = "CARD_TEXT";
	public static String CARD_FOOTNOTE = "CARD_FOOTNOTE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Card welcomeCard = new Card(this);
		welcomeCard.setText(savedInstanceState.getString(CARD_TEXT));
		welcomeCard.setFootnote(savedInstanceState.getString(CARD_FOOTNOTE));
		View view = welcomeCard.getView();
		setContentView(view);
	}
}
