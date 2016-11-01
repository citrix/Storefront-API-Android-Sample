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

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.citrix.developer.storefront_api_android_sample.Classes.Resource;
import com.citrix.developer.storefront_api_android_sample.Tasks.DownloadImageTask;
import com.citrix.developer.storefront_api_android_sample.Tasks.DownloadLaunchICATask;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by John on 6/13/2014.
 */
public class ResourceAdapter extends BaseAdapter
{
    private Activity mContext;
    private ArrayList<Resource> mList;
    private LayoutInflater mLayoutInflater = null;

    public ResourceAdapter(Activity context, ArrayList<Resource> list)
    {
        mContext = context;
        mList = list;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount()
    {
        return mList.size();
    }

    @Override
    public Object getItem(int i)
    {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        final int pos = i;

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        view = inflater.inflate(R.layout.item_layout, viewGroup, false);

        TextView _title = (TextView) view.findViewById(R.id.AppTitle);
        TextView _desc = (TextView) view.findViewById(R.id.AppDesc);
        Button _btn = (Button) view.findViewById(R.id.EditBtn);
        _btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Resource a = mList.get(pos);
                String url = a.AppLaunchURL;
                new DownloadLaunchICATask(mContext.getApplicationContext(), mContext).execute(url);
            }
        });

        Resource a = mList.get(i);
        Drawable _icon = null;
        try
        {
            _icon = new DownloadImageTask(mContext.getApplicationContext(), mContext).execute(a.AppIcon).get();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        if (_icon != null)
        {
            ImageView _appImage = (ImageView) view.findViewById(R.id.AppImage);
            _appImage.setImageDrawable(_icon);
        }
        _title.setText(a.AppTitle);
        _desc.setText(a.AppDesc);

        return view;

    }
}
