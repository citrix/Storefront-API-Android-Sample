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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by John on 6/9/2014.
 */
public class DownloadAuthMethodsTask extends AsyncTask<URL, Void, Void>
{

    Hashtable<String, String> _authLocation = null;
    Context _context = null;
    HashMap<String, String> _authenticationLocation = null;
    String _baseUrL = null;
    String _sessionid = null;
    String _csrfToken = null;
    SharedPreferences _prefs = null;
    Activity _activity = null;
    boolean _useHttps;

    public DownloadAuthMethodsTask(Context Context, Activity Activity)
    {

        _authenticationLocation = new HashMap<String, String>();
        _authLocation = new Hashtable<String, String>();
        this._context = Context;

        _prefs = this._context.getSharedPreferences("AppSettings", 0);
        _baseUrL = _prefs.getString("SFURL", null);
        _sessionid = _prefs.getString("asp.net_sessionid", null);
        _csrfToken = _prefs.getString("csrftoken", null);
        _useHttps = _prefs.getBoolean("UseHttps", false);
        this._activity = Activity;

    }

    @Override
    protected Void doInBackground(URL... urls)
    {

        URL _authMethodsURL = urls[0];


        try
        {
            HttpURLConnection _urlConn = (HttpURLConnection) _authMethodsURL.openConnection();
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
            String _cookieValue = "csrftoken" + "=" + _csrfToken + ";asp.net_sessionid=" + _sessionid;

            _urlConn.setRequestProperty("Cookie", _cookieValue);

            //work around?? (http://stackoverflow.com/questions/17638398/androids-httpurlconnection-throws-eofexception-on-head-requests)
            _urlConn.setRequestProperty("Accept-Encoding", "");
            //set for post
            _urlConn.setDoInput(true);
            _urlConn.setDoOutput(true);


            String line = "";
            String _authMethodsXML = "";
            InputStream response = _urlConn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(response));

            DocumentBuilderFactory _factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder _builder = null;
            try
            {
                _builder = _factory.newDocumentBuilder();
            }
            catch (ParserConfigurationException e)
            {
                e.printStackTrace();
            }

            Document _xmlDoc = null;
            try
            {
                _xmlDoc = _builder.parse(response);
            }
            catch (SAXException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }


            //xpath stuff
            XPath _xPath = XPathFactory.newInstance().newXPath();
            String _pathExpression = "/authMethods/method";
            Map<String, String> _availableAuthMethods = new HashMap<String, String>();


            try
            {
                NodeList _list = (NodeList) _xPath.compile(_pathExpression).evaluate(_xmlDoc, XPathConstants.NODESET);

                for (int nodeCounter = 0; nodeCounter < _list.getLength(); nodeCounter++)
                {
                    Node n = _list.item(nodeCounter);

                    String _name = n.getAttributes().getNamedItem("name").getNodeValue();
                    String _url = n.getAttributes().getNamedItem("url").getNodeValue();

                    if (_name.toLowerCase().equals("postcredentials"))
                    {
                        _authenticationLocation.put(_name, _url);
                    }
                }
            }
            catch (XPathExpressionException e)
            {
                e.printStackTrace();
            }
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
        if (_authenticationLocation.size() == 1)
        {
            for (String key : _authenticationLocation.keySet())
            {
                if (key.toLowerCase().equals("postcredentials"))

                {
                    try
                    {
                        URL _auth = new URL(_baseUrL + _authenticationLocation.get(key));
                        ((MainActivity) this._activity).CallPostCreds(_auth);
                    }
                    catch (MalformedURLException e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    @Override
    protected void onProgressUpdate(Void... values)
    {
        super.onProgressUpdate(values);
    }

}
