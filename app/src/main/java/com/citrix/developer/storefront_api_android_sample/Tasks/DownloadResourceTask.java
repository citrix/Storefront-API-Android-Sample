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

import com.citrix.developer.storefront_api_android_sample.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by John on 6/9/2014.
 */
public class DownloadResourceTask extends AsyncTask<Void, Void, ArrayList<String>>
{
    //String aspnetSessionId;
    //String csrfToken;
    //ArrayList<String> _citrixCookies = new ArrayList<String>();
    URL authMethodsURL;
    Context _context = null;
    String _baseUrL = null;
    String _sessionid = null;
    String _csrfToken = null;
    String _ctxsauthid = null;
    Activity _activity = null;
    String JsonResponse = null;
    boolean _useHttps;

    public DownloadResourceTask(Context Context, Activity Activity)
    {
        this._context = Context;
        SharedPreferences _prefs = this._context.getSharedPreferences("AppSettings", 0);
        _sessionid = _prefs.getString("asp.net_sessionid", null);
        _csrfToken = _prefs.getString("csrftoken", null);
        _ctxsauthid = _prefs.getString("ctxsauthid", null);
        _baseUrL = _prefs.getString("SFURL", null);
        _useHttps = _prefs.getBoolean("UseHttps", false);
        this._activity = Activity;
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings)
    {
        ((MainActivity) this._activity).ParseAndShowResources(JsonResponse);
    }

    @Override
    protected ArrayList<String> doInBackground(Void... voids)
    {
        URL _resourceListURL = null;
        URL _authURL = null;

        try
        {
            _resourceListURL = new URL(_baseUrL + "Resources/List");
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        try
        {
            HttpURLConnection _urlConn = (HttpURLConnection) _resourceListURL.openConnection();
            _urlConn.setRequestMethod("POST");
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
            _urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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
            JsonResponse = "";
            while ((line = reader.readLine()) != null)
            {
                JsonResponse += line;
            }
            Log.d("test", "test");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        //return _citrixCookies;
        return null;
    }

}
