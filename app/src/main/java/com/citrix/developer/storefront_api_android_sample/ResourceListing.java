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

package com.citrix.developer.storefront_api_android_sample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.citrix.developer.storefront_api_android_sample.Classes.Resource;
import com.citrix.developer.storefront_api_android_sample.Tasks.DownloadLaunchICATask;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;


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

public class ResourceListing extends ActionBarActivity
{

    public ArrayList<Resource> _appResources;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //R.layout.activity_resource_listing
        setContentView(R.layout.activity_resource_listing);
        Bundle bndl = this.getIntent().getExtras();
        String JSONResourceList = bndl.getString("JSONRESOURCE");

        //load up the listview.
        _appResources = new ArrayList<Resource>();

        JsonParser parser = new JsonParser();
        JsonElement o = parser.parse(JSONResourceList);
        JsonObject a = o.getAsJsonObject();
        JsonArray _resources = a.get("resources").getAsJsonArray();
        for (JsonElement _resource : _resources)
        {
            JsonObject _obj = _resource.getAsJsonObject();
            Resource _r = new Resource();
            _r.AppTitle = _obj.get("name").getAsString();
            _r.AppLaunchURL = _obj.get("launchurl").getAsString();
            try
            {
                _r.AppDesc = _obj.get("description").getAsString();
            }
            catch (Exception descException)
            {
                //do nothing
            }
            _r.AppIcon = _obj.get("iconurl").getAsString();
            _appResources.add(_r);
        }

        ResourceAdapter _ra = new ResourceAdapter(this, _appResources);

        ListView _lv = (ListView) findViewById(R.id.resourceList);
        _lv.setAdapter(_ra);
        _lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                //get the launch URL
                String url = _appResources.get(i).AppLaunchURL;
                //call the launch task and pass it the launchURL
                new DownloadLaunchICATask(getApplicationContext(), ResourceListing.this).execute(url);
            }
        });
    }

    public void LaunchIcaConnection(String ICAInfo) throws IOException
    {

        try
        {
            File sdcard = Environment.getExternalStorageDirectory();
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadDir.mkdirs();
            File a = new File(downloadDir, "app.ica");
            if (a.exists())
            {
                a.delete();
            }
            else
            {
                a.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(a, true);
            OutputStreamWriter osr = new OutputStreamWriter(fos);
            osr.write(ICAInfo);
            osr.close();

            Intent launchIcaFileIntent = new Intent();
            launchIcaFileIntent.setAction(Intent.ACTION_VIEW);
            launchIcaFileIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            launchIcaFileIntent.setDataAndType(Uri.parse("file://" + a.getPath()), "application/x-ica");
            startActivity(launchIcaFileIntent);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


}
