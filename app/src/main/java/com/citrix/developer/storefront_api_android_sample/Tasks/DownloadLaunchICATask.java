/*
 * ************************************************************************
 *
 *  Copyright (c) 2014-2015 Citrix Systems, Inc. All Rights Reserved.
 *  You may only reproduce, distribute, perform, display, or prepare derivative works of this file pursuant to a valid license from Citrix.
 *
 *  THIS SAMPLE CODE IS PROVIDED BY CITRIX "AS IS" AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *
 * ***********************************************************************
 */

package com.citrix.developer.storefront_api_android_sample.Tasks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.citrix.developer.storefront_api_android_sample.ResourceListing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by John on 6/9/2014.
 */
public class DownloadLaunchICATask extends AsyncTask<String, Void, Void>
{
    Context _context = null;
    Activity _activity = null;
    String _sessionid = null;
    String _csrfToken = null;
    String _ctxsauthid = null;
    String _baseUrL = null;
    String _icaInfo = null;
    boolean _useHttps;

    public DownloadLaunchICATask(Context Context, Activity Activity)
    {
        this._context = Context;
        this._activity = Activity;

        SharedPreferences _prefs = this._context.getSharedPreferences("AppSettings", 0);
        _sessionid = _prefs.getString("asp.net_sessionid", null);
        _csrfToken = _prefs.getString("csrftoken", null);
        _ctxsauthid = _prefs.getString("ctxsauthid", null);
        _baseUrL = _prefs.getString("SFURL", null);
        _useHttps = _prefs.getBoolean("UseHttps", false);
    }

    @Override
    protected Void doInBackground(String... urls)
    {
        URL _launchICAURL = null;


        try
        {
            _launchICAURL = new URL(_baseUrL + urls[0]);

        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        try
        {
            HttpURLConnection _urlConn = (HttpURLConnection) _launchICAURL.openConnection();
            _urlConn.setRequestMethod("GET");
            //Must set this. Needs to match whether or not you are using https on your storefront server
            if (!_useHttps)
            {
                _urlConn.setRequestProperty("X-Citrix-IsUsingHTTPS", "No");
            }
            else
            {
                _urlConn.setRequestProperty("X-Citrix-IsUsingHTTPS", "Yes");
            }

            _urlConn.setRequestProperty("Csrf-Token", _csrfToken);
            _urlConn.setRequestProperty("Content-Type", "application/octet-stream");
            //work around?? (http://stackoverflow.com/questions/17638398/androids-httpurlconnection-throws-eofexception-on-head-requests)
            _urlConn.setRequestProperty("Accept-Encoding", "");
            String _cookieValue = "csrftoken" + "=" + _csrfToken + ";asp.net_sessionid=" + _sessionid + ";CtxsAuthId=" + _ctxsauthid;
            _urlConn.setRequestProperty("Cookie", _cookieValue);

            //set for post
            _urlConn.setDoInput(true);
            _urlConn.setDoOutput(true);


            InputStream response = _urlConn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            String line = null;
            _icaInfo = "";
            while ((line = reader.readLine()) != null)
            {
                _icaInfo += line + "\r\n";
            }
            Log.d("test", "test");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);

        //call method to launch from activity
        try
        {
            ((ResourceListing) this._activity).LaunchIcaConnection(_icaInfo);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


}
