package com.rde.android.barcodecounter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.text.SpannableString
import android.text.util.Linkify
import android.view.InflateException
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


class AboutBox {
    companion object {
        fun VersionName(context: Context): String? {
            return try {
                context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                "Unknown"
            }
        }

        fun Show(callingActivity: Activity) {
            //Use a Spannable to allow for links highlighting
            val aboutText = SpannableString(
                "Version " + VersionName(callingActivity)
                        + callingActivity.getString(R.string.about)
            )
            //Generate views to pass to AlertDialog.Builder and to set the text
            var about: View
            var tvAbout: TextView
            try {
                //Inflate the custom view
                val inflater = callingActivity.layoutInflater
               // var aViewGroup : ViewGroup? = callingActivity.findViewById<View>(R.id.aboutView) as ViewGroup
                about = inflater.inflate(
                    R.layout.about_box,
                    null
                )
                tvAbout = about.findViewById(R.id.aboutText)
            } catch (e: Exception) {
                //Inflater can throw exception, unlikely but default to TextView if it occurs
                e.printStackTrace()

                tvAbout = TextView(callingActivity)
                about = tvAbout
            }
            //Set the about text
            tvAbout.text = aboutText
            // Now Linkify the text
            Linkify.addLinks(tvAbout, Linkify.ALL)
            //Build and show the dialog
            AlertDialog.Builder(callingActivity)
                .setTitle("About " + callingActivity.getString(R.string.app_name))
                .setCancelable(true)
                .setIcon(R.mipmap.ic_launcher )
                .setPositiveButton("OK", null)
                .setView(about)
                .show() //Builder method returns allow for method chaining
        }

    }
}