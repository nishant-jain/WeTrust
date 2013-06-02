/**
 * Copyright 2010-present Facebook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("deprecation")
public class FQLQuery extends Dialog {

    private EditText mFQLQuery;
    private TextView mFQLOutput;
    private Button mSubmitButton;
    private Activity activity;
    private Handler mHandler;
    private ProgressDialog dialog;
    private HashMap<String,Integer> count=new HashMap<String,Integer>();
    private ArrayList<String> post_ids=new ArrayList<String>();

    public FQLQuery(Activity activity) {
        super(activity);
        this.activity = activity;
        setTitle(R.string.fqlquery);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        setContentView(R.layout.fql_query);
        LayoutParams params = getWindow().getAttributes();
        params.width = LayoutParams.FILL_PARENT;
        params.height = LayoutParams.FILL_PARENT;
        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        mFQLQuery = (EditText) findViewById(R.id.fqlquery);
        mFQLOutput = (TextView) findViewById(R.id.fqlOutput);
        mSubmitButton = (Button) findViewById(R.id.submit_button);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(mFQLQuery.getWindowToken(), 0);
                dialog = ProgressDialog.show(FQLQuery.this.activity, "",
                        FQLQuery.this.activity.getString(R.string.please_wait), true, true);
                /*
                 * Source tag: fql_query_tag
                 */
                //if(post_ids.size()==0){
	                String query ="SELECT post_id,actor_id,target_id,message,description,with_tags,created_time,comment_info FROM stream WHERE source_id=me() and actor_id!=me() and created_time>1354320000 LIMIT 5000";
	                //String query=mFQLQuery.getText().toString();
	                Bundle params = new Bundle();
	                params.putString("method", "fql.query");
	                params.putString("query", query);
	                Utility.mAsyncRunner.request(null, params, new FQLRequestListener()); 
	              /*  }
                else{
                	for(int i=0;i<post_ids.size();i++){
                		String query ="SELECT fromid FROM comment WHERE post_id=\""+post_ids.get(i).toString()+"\"";
                        //String query=mFQLQuery.getText().toString();
                        Bundle params = new Bundle();
                        params.putString("method", "fql.query");
                        params.putString("query", query);
                        Utility.mAsyncRunner.request(null, params, new FQLRequestListener()); 
                	}
                }*/
            }
        });
    }

    public class FQLRequestListener extends BaseRequestListener {

        @Override
        public void onComplete(final String response, final Object state) {
            dialog.dismiss();
            /*
             * Output can be a JSONArray or a JSONObject.
             * Try JSONArray and if there's a JSONException, parse to JSONObject
             */
            try {
            	
                JSONArray json = new JSONArray(response);
               // JSONObject jsonobj= new JSONObject(response);
                //if(post_ids.size()==0){
	                for(int i=0; i<json.length();i++){
	                	JSONObject newobj = json.getJSONObject(i);
	                	String actor_id=newobj.getString("actor_id");
	                	String postid= newobj.getString("post_id");
	                	post_ids.add(postid);
	                	System.out.println(newobj.toString());
	                	
	                	if(count.containsKey(actor_id)){
	                		//int a= Integer.valueOf(count.get(actor_id).toString())+1;
	                		count.put(actor_id, count.get(actor_id)+1 );
	                	}
	                	else{
	                		count.put(actor_id,1);
	                	}
	                	
	                }
            /*	}
        	else{
            		
               //	JSONObject newobj = new JSONObject(response);
                	for(int i=0;i<json.length();i++){
                		JSONObject from_idnew=json.getJSONObject(i);
                		String from_id=from_idnew.getString("fromid");
                		Log.e("from_id",from_id);
            		if(count.containsKey(from_id)){
	                		//int a= Integer.valueOf(count.get(actor_id).toString())+1;
	                		count.put(from_id, count.get(from_id)+1 );
	                	}
	                else{
	                		count.put(from_id,1);
	                	}
                	}
            	}*/
              
                
                Iterator iterator = count.keySet().iterator();  
                String result="";

                while (iterator.hasNext()) {  
                   String key = iterator.next().toString();  
                   String value = count.get(key).toString();  
                   result+=key + ":" + value+"\n";
                   System.out.println(key + " " + value);  
                }  
                setText(result);
            } catch (JSONException e) {
                try {
                    /*
                     * JSONObject probably indicates there was some error
                     * Display that error, but for end user you should parse the
                     * error and show appropriate message
                     */
                    JSONObject json = new JSONObject(response);
                    setText(json.toString(2));
                } catch (JSONException e1) {
                    setText(activity.getString(R.string.exception) + e1.getMessage());
                }
            }
        }

        public void onFacebookError(FacebookError error) {
            dialog.dismiss();
            setText(activity.getString(R.string.facebook_error) + error.getMessage());
        }
    }

    public void setText(final String txt) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mFQLOutput.setText(txt);
                mFQLOutput.setVisibility(View.VISIBLE);
            }
        });
    }
}
