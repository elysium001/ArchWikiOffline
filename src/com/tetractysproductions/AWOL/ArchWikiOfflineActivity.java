/** 
 * Copyright (C) 2012 Tetractys Productions LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Exiquio Cooper-Anderson (exiquio [at] gmail [dot] com) 
 * 
 */


package com.tetractysproductions.AWOL;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class ArchWikiOfflineActivity extends Activity {
	// CONSTANTS
	private static String TAG = "AWOL - AWO_A";
	
	// PRIVATE INSTANCE VARIABLES
	private Context context;
    private File config_dir;
	private String wiki_filepath;
	private ProgressDialog dialog;
	private String topic;
	
	// LIFECYCLE CALLBACKS
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	
        Log.d(TAG, "ArchWikiOfflineActivity created1...");
        context = this;
        Log.d(TAG, "context: " + context);
        config_dir = new File(context.getFilesDir().getAbsoluteFile() + "/awol");
        Log.d(TAG, "config_dir: " + config_dir.getAbsolutePath());
        wiki_filepath = config_dir.getAbsolutePath() + "/archwiki.zim";
        dialog = new ProgressDialog(context);
   
        ConfigSetupTask task = new ConfigSetupTask();
        task.execute();
        
        Spinner topic_spinner = (Spinner) findViewById(R.id.topic_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
        		context,
        		R.array.topics,
        		android.R.layout.simple_spinner_item
        	);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        topic_spinner.setAdapter(adapter);
        topic_spinner.setOnItemSelectedListener(new TopicSelectedListener());
    }
	
	// PUBLIC INSTANCE METHODS
	@Override
    public boolean onSearchRequested() {
		Log.d(TAG, "onSearchRequested called...");
		Log.d(TAG, "loading bundle...");
    	Bundle app_data = new Bundle();
    	app_data.putString("wiki_filepath", wiki_filepath);
    	Log.d(TAG, "bundle loaded!");
    	Log.d(TAG, "calling startSearch...");
        startSearch(null, false, app_data, false);
        Log.d(TAG, "startSearch called!");
        Log.d(TAG, "completed response to search request!");
        return true;
    }

	public void searchWiki(View view) {
    	Log.d(TAG, "searchWiki called");
    	Log.d(TAG, "requesting search...");
    	onSearchRequested();
    	Log.d(TAG, "search requested!");
    }
    
	public void displayPage(View view) {
    	Log.d(TAG, "displayPage called...");
    	Log.d(TAG, "wiki topic: " + topic);
    	Intent intent = new Intent(context, DisplayPageActivity.class);
    	Log.d(TAG, "loading extras...");
    	intent.putExtra("wiki_filepath", wiki_filepath);
    	if(topic.equals("Index")) {
    		intent.putExtra("page_title", "index");
    	} else {
    		intent.putExtra("page_title", topic + " - ArchWiki.html");
    	}
    	// TODO ensure that topic is accurate (exiquio)
    	Log.d(TAG, "extras packed!");
    	Log.d(TAG, "calling DisplayPageActivity...");
    	startActivity(intent);
    	Log.d(TAG, "displayPage done!");
    }
    
    // PRIVATE INNER CLASSES
    private class ConfigSetupTask extends AsyncTask<Void, Void, Void> {
		@Override 
		protected void onPreExecute() {
			Log.d(TAG, "starting config setup task...");
			dialog.setMessage("Configuring. Please wait...");
			dialog.setCancelable(false);
			dialog.show();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			Log.d(TAG, "config_dir: " + config_dir);
			Log.d(TAG, "does config dir exist on filesystem?");
	        if(!config_dir.exists()) {
	        	Log.d(TAG, "config dir does not exist on filesystem!");
	        	Log.d(TAG, "creating config dir on filesystem...");
	        	config_dir.mkdirs();
	        	Log.d(TAG, "config dir created on filesystem!");
	        }
	        Log.d(TAG, "config dir does exist on filesystem.");
	        
	        Log.d(TAG, "does archwiki.zim exist in config dir?");
	        File wikifile = new File(config_dir, "archwiki.zim"); 
	        // FIXME: file needs version'd name, replace op and md5sum check (exiquio)
	        if(!wikifile.exists()) {
	        	Log.d(TAG, "archwiki.zim does not exist in config dir!");
	        	Log.d(TAG, "copying archwiki.zim to config dir...");
	        	try {
	        		FileOutputStream wiki_OS = new FileOutputStream(wiki_filepath, false);
	        		OutputStream os = new BufferedOutputStream(wiki_OS);
	        		byte[] buffer = new byte[1024];
	        		int byte_read = 0;
	        		InputStream wiki_IS = context.getResources().openRawResource(R.raw.archwiki);
	        		while((byte_read = wiki_IS.read(buffer)) != -1) {
	        			os.write(buffer, 0, byte_read);
	        		}
	        		wiki_OS.close();
	        		wiki_IS.close();
	        	} catch(Exception e) {
	        		Log.d(TAG, "archwiki.zim has been copied to config dir!");
	        		e.printStackTrace(); // TODO: is this the best way to handle such? (exiquio)
	        	}
	        }
	        Log.d(TAG, "archwiki.zim does exist in config dir.");
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			Log.v(TAG, "config setup task complete");
			dialog.dismiss();
		}
    }
    
    // PUBLIC INNER CLASSES
    public class TopicSelectedListener implements OnItemSelectedListener {
    	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
    		topic = parent.getItemAtPosition(pos).toString();
        }

        public void onNothingSelected(AdapterView<?> parent) {
        	// DO NOTHING 
        	// TODO is this appropriate? (exiquio)
        }
    }
}