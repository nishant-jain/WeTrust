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

/*
 * The me, delete and back_parent buttons are downloaded from http://icongal.com/
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class GraphExplorer extends Activity {
    private Button mSubmitButton, mViewURLButton;
    private Button mGetPermissionsButton;
    private Button mTextDeleteButton, mMeButton;
    private Button mFieldsConnectionsButton, mBackParentButton;
    private TextView mOutput;
    private EditText mInputId;
    private Bundle params;
    private String url, mParentObjectId;
    private ProgressDialog dialog;
    private String rootString;
    private ScrollView mScrollView;
    private Handler mHandler;
    private final static String BASE_GRAPH_URL = "https://graph.facebook.com";
    private HashMap<String,Integer> count=new HashMap<String,Integer>();

    private JSONObject metadataObject;

    /*
     * Layout the Graph Explorer
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        setContentView(R.layout.graph_explorer);

        url = BASE_GRAPH_URL; // Base URL

        mInputId = (EditText) findViewById(R.id.inputId);
        mOutput = (TextView) findViewById(R.id.output);
        mSubmitButton = (Button) findViewById(R.id.submitButton);
        mViewURLButton = (Button) findViewById(R.id.viewURLButton);
        mGetPermissionsButton = (Button) findViewById(R.id.accessTokenButton);
        mFieldsConnectionsButton = (Button) findViewById(R.id.fieldsAndConnectionsButton);
        mBackParentButton = (Button) findViewById(R.id.backParentButton);

        mScrollView = (ScrollView) findViewById(R.id.ScrollView01);

        mTextDeleteButton = (Button) findViewById(R.id.textDeleteButton);
        mMeButton = (Button) findViewById(R.id.meButton);
        if (Utility.mFacebook.isSessionValid()) {
            mMeButton.setVisibility(View.VISIBLE);
        }

        params = new Bundle();
        mSubmitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(mInputId.getWindowToken(), 0);

                // Prepare the URL to be shown on 'View URL' click action. This
                // is not used by the SDK
                url = BASE_GRAPH_URL; // Base URL

                /*
                 * Source Tag: graph_explorer
                 */
                rootString = mInputId.getText().toString();
                //rootString = "me/photos";
                if (!TextUtils.isEmpty(rootString)) {
                    dialog = ProgressDialog.show(GraphExplorer.this, "",
                            getString(R.string.please_wait), true, true);
                    params.putString("metadata", "1");
                    Utility.mAsyncRunner.request(rootString, params, new graphApiRequestListener());
                    url += "/" + rootString; // Relative Path provided by you
                }

            }
        });

        mViewURLButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setText(url);
                Linkify.addLinks(mOutput, Linkify.WEB_URLS);
            }
        });

        mGetPermissionsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utility.mFacebook.isSessionValid()) {
                    dialog = ProgressDialog.show(GraphExplorer.this, "",
                            getString(R.string.fetching_current_permissions), true, true);
                    Bundle params = new Bundle();
                    params.putString("access_token", Utility.mFacebook.getAccessToken());
                    Utility.mAsyncRunner.request("me/permissions", params,
                            new permissionsRequestListener());
                } else {
                    new PermissionsDialog(GraphExplorer.this).show();
                }
            }
        });

        mFieldsConnectionsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (metadataObject == null) {
                    makeToast("No fields, connections availalbe for this object.");
                } else {
                    new FieldsConnectionsDialog(GraphExplorer.this, metadataObject).show();
                }
            }
        });

        mTextDeleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                url = BASE_GRAPH_URL; // Base URL
                mParentObjectId = "";
                mInputId.setText("");
                params.clear();
                metadataObject = null;
                setText("");
                mBackParentButton.setVisibility(View.INVISIBLE);
            }
        });

        mMeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputId.setText("me/photos");
                params.putString("limit", "50");
                params.putString("fields","id,link,comments,from,tags");
                
                //me?fields=photos.limit(50).fields(link,comments.limit(100).fields(from,id),from,tags.limit(50).fields(id,name))

                mSubmitButton.performClick();
            }
        });

        mBackParentButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputId.setText(mParentObjectId);
                mParentObjectId = "";
                mSubmitButton.performClick();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Utility.mFacebook.isSessionValid()) {
            mMeButton.setVisibility(View.VISIBLE);
        }
        if (Utility.objectID != null) {
            mInputId.setText(Utility.objectID);
            Utility.objectID = null;
            mSubmitButton.performClick();
        }
    }

    protected void processIntent(Intent incomingIntent) {
        Uri intentUri = incomingIntent.getData();
        if (intentUri == null) {
            return;
        }
        String objectID = intentUri.getHost();
        mInputId.setText(objectID);
        mSubmitButton.performClick();
    }

    public void getConnection(String connection) {
        mInputId.setText(rootString + "/" + connection);
        mParentObjectId = rootString;
        mSubmitButton.performClick();
    }

    public void getFields(Vector<String> fieldsVector) {
        String fields = "";
        int count = 0;
        for (String field : fieldsVector) {
            fields += field;
            if (++count < fieldsVector.size()) {
                fields += ",";
            }
        }
        params.putString("fields", fields);
        mSubmitButton.performClick();
    }

    /*
     * Callback for the permission OAuth Dialog
     */
    public class permissionsRequestListener extends BaseRequestListener {

        @Override
        public void onComplete(final String response, final Object state) {
            dialog.dismiss();
            /*
             * Clear the current permission list and repopulate with new
             * permissions. This is used to mark assigned permission green and
             * unclickable.
             */
            Utility.currentPermissions.clear();
            try {
                JSONObject jsonObject = new JSONObject(response).getJSONArray("data")
                        .getJSONObject(0);
                Iterator<?> iterator = jsonObject.keys();
                String permission;
                while (iterator.hasNext()) {
                    permission = (String) iterator.next();
                    Utility.currentPermissions.put(permission,
                            String.valueOf(jsonObject.getInt(permission)));
                }
            } catch (JSONException e) {
                makeToast("Permissions could not be fetched, none will be selected by default.");
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    new PermissionsDialog(GraphExplorer.this).show();
                }
            });
        }

        public void onFacebookError(FacebookError error) {
            dialog.dismiss();
            makeToast("Permissions could not be fetched, none will be selected by default.");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    new PermissionsDialog(GraphExplorer.this).show();
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Utility.mFacebook.authorizeCallback(requestCode, resultCode, data);
    }

    /*
     * Callback after a given Graph API request is executed Get the response and
     * show it.
     */
    public class graphApiRequestListener extends BaseRequestListener {

        @Override
        public void onComplete(final String response, final Object state) {
            dialog.dismiss();
            // access token is appended by Facebook object, hence params are
            // added here after request is complete
            if (!params.isEmpty()) {
                url += "?" + Util.encodeUrl(params); // Params
            }
            metadataObject = null;
  
            params.clear();
            try {
                JSONObject json = Util.parseJson(response);
                if (json.has("metadata")) {
                    metadataObject = json.getJSONObject("metadata");
                    json.remove("metadata");
                } else {
                    metadataObject = null;
                }
                Log.e("original",json.toString(2));
                Log.e("data",json.names().toString());
                JSONArray data = json.getJSONArray("data");	
                
                //Log.e("tags",tags.names().toString());
                Log.e("Length",""+data.length());
                for(int i=0; i<data.length();i++){
                	JSONObject tags= data.getJSONObject(i);	
                  
                Log.e("tags",tags.names().toString());
                   	
                   	JSONObject tags_real= tags.getJSONObject("tags");
                   	JSONArray tagsdata=tags_real.getJSONArray("data");
                   	

                    JSONObject comments = tags.optJSONObject("comments");
                    JSONArray commentdata=null;
                  	if(comments!=null)
                  		 commentdata=comments.getJSONArray("data");
                   	
                	JSONObject from= tags.getJSONObject("from");
                	String fromid=from.getString("id");
                	if(tagsdata.length()<8){
                		if(count.containsKey(fromid)){
                    		//int a= Integer.valueOf(count.get(actor_id).toString())+1;
                    		count.put(fromid, count.get(fromid)+1 );
                    	}
                    	else{
                    		count.put(fromid,1);
                    	}
                		
                		for(int j=0;j<tagsdata.length();j++){
                			String id=tagsdata.getJSONObject(j).getString("id");
                			if(count.containsKey(id)){
                        		//int a= Integer.valueOf(count.get(actor_id).toString())+1;
                        		count.put(id, count.get(id)+1 );
                        	}
                        	else{
                        		count.put(id,1);
                        	}	
                		}
                		if(commentdata!=null)
                		for(int j=0;j<commentdata.length();j++){
                			String id=commentdata.getJSONObject(j).getJSONObject("from").getString("id");
                			if(count.containsKey(id)){
                        		//int a= Integer.valueOf(count.get(actor_id).toString())+1;
                        		count.put(id, count.get(id)+1 );
                        	}
                        	else{
                        		count.put(id,1);
                        	}	
                		}
                	
                	}
                	
                }
                	Iterator iterator = count.keySet().iterator();  
                    String result="";
                    while (iterator.hasNext()) {  
                       String key = iterator.next().toString();  
                       String value = count.get(key).toString();  
                       result+=key + ":" + value+"\n";
                       System.out.println(key + " " + value);  
                    }  
                		
                		
                	
                //	String postid= tags_real.getString("post_id");
                	//post_ids.add(postid);
                	/*System.out.println(newobj.toString());
                	
                	*/
                	//System.out.println(tags.toString());
               // }
                setText(result);
            } catch (JSONException e) {
                setText(e.getMessage());
                e.printStackTrace();
            } catch (FacebookError e) {
                setText(e.getMessage());
                e.printStackTrace();
            }
        }

        public void onFacebookError(FacebookError error) {
            dialog.dismiss();
            setText(error.getMessage());
            params.clear();
            metadataObject = null;
        }

    }

    public void setText(final String txt) {
        mHandler.post(new Runnable() {

            /*
             * A transform filter that simply returns just the text captured by
             * the first regular expression group.
             */
            TransformFilter idFilter = new TransformFilter() {
                @Override
                public final String transformUrl(final Matcher match, String url) {
                  // Log.e(match.group(1), match.group(1)); 
                   return match.group(1);
                }
            };

            @Override
            public void run() {
                mViewURLButton.setVisibility(
                        TextUtils.isEmpty(txt) ? View.INVISIBLE : View.VISIBLE);
                mFieldsConnectionsButton.setVisibility(TextUtils.isEmpty(txt) ? View.INVISIBLE
                        : View.VISIBLE);
                mOutput.setVisibility(TextUtils.isEmpty(txt) ? View.INVISIBLE : View.VISIBLE);
                mBackParentButton.setVisibility(
                        TextUtils.isEmpty(mParentObjectId) ? View.INVISIBLE : View.VISIBLE);

                String convertedTxt = txt.replace("\\/", "/");
                mOutput.setText(convertedTxt);
                mScrollView.scrollTo(0, 0);

                Linkify.addLinks(mOutput, Linkify.WEB_URLS);
                /*
                 * Linkify the object ids so they can be clicked. match pattern:
                 * "id" : "objectid" (objectid can be int or int_int)
                 */
                Pattern pattern = Pattern.compile("\"id\": \"(\\d*_?\\d*)\"");
                String scheme = "fbGraphEx://";
                Linkify.addLinks(mOutput, pattern, scheme, null, idFilter);
            }
        });
    }

    private void makeToast(final String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(GraphExplorer.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
