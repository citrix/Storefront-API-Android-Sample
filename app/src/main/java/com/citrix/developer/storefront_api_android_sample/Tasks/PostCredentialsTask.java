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

import com.citrix.developer.storefront_api_android_sample.MainActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 6/9/2014.
 */
public class PostCredentialsTask extends AsyncTask<URL, Void, Void>
{
    ArrayList<String> _citrixCookies;
    Context _context = null;
    String _ctxsauthid = null;
    String _sessionid = null;
    String _csrfToken = null;
    SharedPreferences _prefs = null;
    Activity _activity = null;
    String _username = null;
    String _password = null;
    boolean _useHttps;

    public PostCredentialsTask(String Username, String Password, Context Context, Activity Activity)
    {
        this._context = Context;
        _prefs = this._context.getSharedPreferences("AppSettings", 0);
        _sessionid = _prefs.getString("asp.net_sessionid", null);
        _csrfToken = _prefs.getString("csrftoken", null);
        _useHttps = _prefs.getBoolean("UseHttps", false);
        this._activity = Activity;
        this._username = Username;
        this._password = Password;

    }

    @Override
    protected void onPostExecute(Void aVoid)
    {
        super.onPostExecute(aVoid);
        ((MainActivity) this._activity).CallDownloadResources();
    }

    @Override
    protected void onProgressUpdate(Void... values)
    {
        super.onProgressUpdate(values);
    }

    @Override
    protected Void doInBackground(URL... urls)
    {
        URL _postCredentialURL = urls[0];

        HttpURLConnection _urlConn = null;
        try
        {
            _urlConn = (HttpURLConnection) _postCredentialURL.openConnection();
            _urlConn.setRequestMethod("POST");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

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
        String _cookieValue = "csrftoken" + "=" + _csrfToken + ";asp.net_sessionid=" + _sessionid;

        _urlConn.setRequestProperty("Cookie", _cookieValue);
        //work around?? (http://stackoverflow.com/questions/17638398/androids-httpurlconnection-throws-eofexception-on-head-requests)
        _urlConn.setRequestProperty("Accept-Encoding", "");

        //set for post
        _urlConn.setDoInput(true);
        _urlConn.setDoOutput(true);

        String _body = "username=" + _username + "&password=" + _password;
        byte[] _bodyBytes = new byte[0];
        try
        {
            _bodyBytes = _body.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        OutputStream _os = null;
        try
        {
            _os = _urlConn.getOutputStream();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            _os.write(_bodyBytes);
            _os.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        InputStream response = null;
        try
        {
            response = _urlConn.getInputStream();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(response));

        //check the response for success and then parse the cookies/headers for the CtxsAuthId cookie
        List cookieList = _urlConn.getHeaderFields().get("Set-Cookie");
        if (cookieList != null)
        {
            for (Object cookieTemp : cookieList)
            {
                String[] _cookieParse = cookieTemp.toString().split(";");
                String[] cookieBreakdown = _cookieParse[0].split("=");
                if (cookieBreakdown[0].toLowerCase().equals("ctxsauthid"))
                {
                    _ctxsauthid = cookieBreakdown[1].toString();
                }
            }
        }
        SharedPreferences _prefs = this._context.getSharedPreferences("AppSettings", 0);
        SharedPreferences.Editor _edit = _prefs.edit();
        _edit.putString("ctxsauthid", _ctxsauthid);
        _edit.commit();

        return null;
    }
}
