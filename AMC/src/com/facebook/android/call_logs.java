package com.facebook.android;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.os.Bundle;
import android.provider.CallLog;
import android.app.Activity;
import android.database.Cursor;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class call_logs extends Activity {
	
	public String getCallDetails() {

		StringBuffer sb = new StringBuffer();
		Cursor managedCursor = managedQuery( CallLog.Calls.CONTENT_URI,null, null,null, null);
		int number = managedCursor.getColumnIndex( CallLog.Calls.NUMBER ); 
		int type = managedCursor.getColumnIndex( CallLog.Calls.TYPE );
		int date = managedCursor.getColumnIndex( CallLog.Calls.DATE);
		int duration = managedCursor.getColumnIndex( CallLog.Calls.DURATION);
		//sb.append( "Call Details :");
		HashMap<String,Integer> call_durations=new HashMap<String,Integer>();
		HashMap<String,Integer> call_logs=new HashMap<String,Integer>();
		while ( managedCursor.moveToNext() ) {
			String phNumber = managedCursor.getString( number );
			String callType = managedCursor.getString( type );
			String callDate = managedCursor.getString( date );
			Date callDayTime = new Date(Long.valueOf(callDate));
			String callDuration = managedCursor.getString( duration );
			String dir = null;
			int dircode = Integer.parseInt( callType );
			switch( dircode ) {
			case CallLog.Calls.OUTGOING_TYPE:
				dir = "OUTGOING";
				break;

			case CallLog.Calls.INCOMING_TYPE:
				dir = "INCOMING";
				break;

			case CallLog.Calls.MISSED_TYPE:
				dir = "MISSED";
				break;
			}
			//sb.append( "\nPhone Number:--- "+phNumber +" \nCall Type:--- "+dir+" \nCall Date:--- "+callDayTime+" \nCall duration in sec :--- "+callDuration );
			//sb.append("\n----------------------------------");
			if(Integer.parseInt(callDuration)!=0){
				if(call_durations.containsKey(phNumber)){
					int value=call_durations.get(phNumber);
					value+=Integer.parseInt(callDuration);
					call_durations.put(phNumber, value);
					value=call_logs.get(phNumber);
					value++;
					call_logs.put(phNumber, value);
				}
				else{
					int value=Integer.parseInt(callDuration);
					call_durations.put(phNumber, value);
					call_logs.put(phNumber, 1);
				}
			}
		}
		managedCursor.close();
		Iterator it = call_durations.entrySet().iterator();
		int count=0;
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			sb.append( "\nPhone Number:--- "+pairs.getKey() +" \nCall duration in sec :--- "+pairs.getValue() +" \nCall count :--- "+call_logs.get(pairs.getKey()));
			sb.append("\n----------------------------------");
			Clustering.phNumber.add((String) pairs.getKey());
			Clustering.xaxis.add(Double.parseDouble(call_durations.get(pairs.getKey()).toString()));
			Clustering.yaxis.add(Double.parseDouble(call_logs.get(pairs.getKey()).toString()));
			//count++;
		}
		Clustering c=new Clustering();
		//c.main();
		//return sb.toString();
		return sb.append(c.main()).toString();
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.call_logs);
		//setContentView(R.layout.call);
		//TextView call = (TextView)findViewById(R.id.call);
		//getCallDetails();
		LinearLayout lView = new LinearLayout(this);

		TextView myText = new TextView(this);
		myText.setText(getCallDetails());
		
		lView.addView(myText);
		ScrollView s=new ScrollView(this);
		s.addView(lView);
		setContentView(s);

	}




}