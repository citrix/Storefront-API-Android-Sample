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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 6/9/2014.
 */
public class CheckAuthenticationTask extends AsyncTask<Void, Void, ArrayList<String>>
{
    URL authMethodsURL;
    Context _context = null;
    String _baseUrL = null;
    Activity _activity = null;

    public CheckAuthenticationTask(Context Context, Activity Activity)
    {
        this._context = Context;
        this._activity = Activity;
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings)
    {
        ((MainActivity) this._activity).CallDownloadAuthMethods(authMethodsURL);
    }

    @Override
    protected ArrayList<String> doInBackground(Void... voids)
    {
        URL _resourceListURL = null;
        SharedPreferences _prefs = this._context.getSharedPreferences("AppSettings", 0);
        boolean _useHttps = _prefs.getBoolean("UseHttps", false);

        _baseUrL = _prefs.getString("SFURL", null);

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
            //work around?? (http://stackoverflow.com/questions/17638398/androids-httpurlconnection-throws-eofexception-on-head-requests)
            _urlConn.setRequestProperty("Accept-Encoding", "");
            //set for post
            _urlConn.setDoInput(true);
            _urlConn.setDoOutput(true);

            String _response = _urlConn.getResponseMessage();

            String headerCitrixAuthCheck = _urlConn.getHeaderField("CitrixWebReceiver-Authenticate");

            String aspnetSessionId = null;
            String csrfToken = null;

            List cookieList = _urlConn.getHeaderFields().get("Set-Cookie");
            if (cookieList != null)
            {
                for (Object cookieTemp : cookieList)
                {
                    String[] _cookieParse = cookieTemp.toString().split(";");
                    String[] cookieBreakdown = _cookieParse[0].split("=");
                    if (cookieBreakdown[0].toLowerCase().equals("asp.net_sessionid"))
                    {
                        aspnetSessionId = cookieBreakdown[1].toString();
                    }
                    else if (cookieBreakdown[0].toLowerCase().equals("csrftoken"))
                    {
                        csrfToken = cookieBreakdown[1].toString();
                    }


                    Log.d("test", "test");
                }
                SharedPreferences.Editor _edit = _prefs.edit();
                _edit.putString("asp.net_sessionid", aspnetSessionId);
                _edit.putString("csrftoken", csrfToken);
                _edit.commit();
            }

            if (headerCitrixAuthCheck != null)
            {
                String[] citrixAuthCheckHeader = headerCitrixAuthCheck.split(",");
                String[] reasonString = citrixAuthCheckHeader[0].split("=");
                String[] locationString = citrixAuthCheckHeader[1].split("=");

                authMethodsURL = new URL(_baseUrL + locationString[1].replace("\"", ""));
            }
            else
            {
                authMethodsURL = null;
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

}
