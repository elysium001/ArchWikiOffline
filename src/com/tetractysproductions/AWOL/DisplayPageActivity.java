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

import java.net.URLEncoder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tetractysproductions.OfflineWiki.OfflineWikiReader;

public class DisplayPageActivity extends Activity {
	// CONSTANTS
	private static String TAG = "AWOL - LPA";
	
	// PRIVATE INSTANCE VARIABLES
	private Context context;
	private ProgressDialog dialog;
	private String wiki_filepath;

	// LIFECYCLE CALLBACKS
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		Log.d(TAG, "DisplayPageActivity created...");
    	context = this;
        dialog = new ProgressDialog(context);
	    setContentView(R.layout.display_page);

	    Log.d(TAG, "unloading extras...");
	    Bundle extras = getIntent().getExtras(); // FIXME This is not checking for null and it should. (exiquio)
	    wiki_filepath = extras.getString("wiki_filepath");
	    Log.d(TAG, "wiki_filepath: " + wiki_filepath);
	    String page_title = extras.getString("page_title");
	    Log.d(TAG, "page_title: " + page_title);
	    Log.d(TAG, "extras unloaded!");
		
	    Log.d(TAG, "getting wiki page...");
		LoadWikiPageTask task = new LoadWikiPageTask();
		task.execute(page_title);
		Log.d(TAG, "finished getting wiki page!");
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
    
	private String getWikiPage(String wiki_filepath, String page_title) {
		String results = null;
		OfflineWikiReader owr = new OfflineWikiReader(wiki_filepath);
		if(page_title.equals("index")) {
			results = owr.getIndex();
		} else {
			results = owr.getPage(page_title);
		}
		Log.d(TAG, "results length: " + results.length());
		return results;
	}

	// PRIVATE INNER CLASSES
	private class WikiWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
	          Log.d(TAG, "URL requested...");
	          Log.d(TAG, "URL: " + url);
	          if(url.contains("http://")) { // FIXME Not sure if this is what we are looking for (exiquio)
	        	  Intent i = new Intent(Intent.ACTION_VIEW);
	        	  i.setData(Uri.parse(url));
	        	  startActivity(i);
	          } else {
	        	  Log.d(TAG, "The last url requested did not contain 'http'");
	          }
	          return true;
		}
		
	    @Override
	    public void onPageFinished(WebView view, String url) {
	    	Log.d(TAG, "performing JavaScript operations...");
			FormatMarkupTask task = new FormatMarkupTask();
			task.execute(view);	
	        Log.d(TAG, "JavaScript operations performed!");
	    }
	}
	
	private class FormatMarkupTask extends AsyncTask<WebView, Void, String> {
		private WebView view; 
		
		@Override
		protected void onPreExecute() {
			Log.d(TAG, "FomrmatMarkupTask started...");
			setProgressBarIndeterminateVisibility(true);
			dialog.setMessage("Formating markup... Please wait...");
			dialog.setCancelable(false);
			dialog.show();
		}
		
		@Override
		protected String doInBackground(WebView... views) {
	    	view = views[0];
			Log.d(TAG, "creating scripts...");
	    	// FIXME: Perform the following operations in a more standard oriented way. (exiquio)
	    	String script_1 = "document.getElementById('archnavbarmenu').innerHTML='';";
	    	String script_2 = "document.getElementById('jump-to-nav').innerHTML='';";
	    	String script_3 = "document.getElementById('column-one').innerHTML='';";
	    	String script_4 = "document.getElementById('footer').innerHTML='<p>Content is available under GNU Free Documentation License 1.2<p>'";     
	        Log.d(TAG, "scripts created!");
	    	return "javascript:" + script_1 + script_2 + script_3 + script_4;
		}
		
		@Override
		protected void onPostExecute(String script) {
			Log.d(TAG, "running scripts...");
			view.loadUrl(script);
			Log.d(TAG, "scripts completed!");
			Log.d(TAG, "FormatMarkupTask completed!");
			dialog.dismiss(); 
		}
	}
	
	private class LoadWikiPageTask extends AsyncTask<String, Void, String> {
		@Override 
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			dialog.setMessage("Loading page. Please wait...");
			dialog.setCancelable(false);
			dialog.show();
		}
		
		@Override
		protected String doInBackground(String... page_titles) {
			String html = null;
			html = getWikiPage(wiki_filepath, page_titles[0]);
		    return html;
		}
		
		@Override
		protected void onPostExecute(String html) {
			Log.d(TAG, "loading webview...");
		    WebView webview = (WebView) findViewById(R.id.webview);
		    webview.setWebViewClient(new WikiWebViewClient()); //FIXME: This doesn't seem to intercept we need. (exiquio)
		    webview.setBackgroundColor(Color.parseColor("#F6F9FC"));
		    WebSettings webview_settings = webview.getSettings();
		    //webview_settings.setAllowContentAccess(false); // FIXME: requires API 11 (exiquio)
		    webview_settings.setAllowFileAccess(false); 
		    //webview_settings.setDisplayZoomControls(false); FIXME: requires API 11 (exiquio)
		    webview_settings.setJavaScriptEnabled(true);
		    webview_settings.setLoadWithOverviewMode(true); // TODO: answer SO question (http://stackoverflow.com/questions/1991219/android-webview-wrap-content) (exiquio)
		    webview_settings.setUseWideViewPort(true);
		    webview_settings.setBuiltInZoomControls(true);
		    webview.loadData(URLEncoder.encode(html).replaceAll("\\+"," "), "text/html", "utf-8");
		    Log.d(TAG, "webview loaded!");
		    //dialog.dismiss(); Dismissed in FormatMarkupTask (exiquio)
		}
	}
}


// FIXME: This app would crash on certain changes that cause the app to be killed and restarted. At the moment the app is
// using a work around in AndroidManifest.xml (android:configChanges="orientation|keyboardHidden"). This is just masking
// the problem and should be corrected like the example shown at the following URL:
//
// http://stackoverflow.com/questions/1111980/how-to-handle-screen-orientation-change-when-progress-dialog-and-background-thre
// (exiquio)

// FIXME Known Issues:
// -Local URLs do not load anything useful. (exiquio)