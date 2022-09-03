package io.github.xSocks.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import io.github.xSocks.R;
import io.github.xSocks.aidl.Config;
import io.github.xSocks.model.Profile;
import io.github.xSocks.preferences.PasswordEditTextPreference;
import io.github.xSocks.preferences.ProfileEditTextPreference;
import io.github.xSocks.preferences.SummaryEditTextPreference;
import io.github.xSocks.utils.ConfigUtils;
import io.github.xSocks.utils.Constants;
import io.github.xSocks.utils.Utils;

public class PrefsFragment extends PreferenceFragment {
    public static String[] PROXY_PREFS = {
            Constants.Key.profileName,
            Constants.Key.proxy,
            Constants.Key.remotePort,
            Constants.Key.localPort,
            Constants.Key.protocol,
            Constants.Key.sitekey,
    };

    public static String[] FEATURE_PREFS = {
            Constants.Key.route,
            Constants.Key.isGlobalProxy,
            Constants.Key.proxyedApps,
            Constants.Key.isUdpDns,
            Constants.Key.isAutoConnect
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Config config= ConfigUtils.load( this.getPreferenceManager().getSharedPreferences());
        addPreferencesFromResource(R.xml.preferences);

        findPreference("protocol").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if( o.equals("wss")||o.equals("http2")) {
                    findPreference("caFile").setEnabled(true);
                    findPreference("verifycert").setEnabled(true);
                }else{
                    findPreference("verifycert").setEnabled(false);
                    findPreference("caFile").setEnabled(false);
                }
                findPreference("tuntype").setEnabled(true);
                return true;
            }
        });
        findPreference("caFile").setEnabled(false);
        findPreference("verifycert").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if( o.equals("self_signed")) {
                    findPreference("caFile").setEnabled(true);
                }else{
                    findPreference("caFile").setEnabled(false);
                }
                return true;
            }
        });

        if(config.verifyCert.equals("self_signed")){
            findPreference("caFile").setEnabled(true);
        }else{
            findPreference("caFile").setEnabled(false);
        }
        findPreference("caFile").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                fileChooser();
                return false;
            }
        });
        findPreference("caFile").setSummary(config.caFile);
    }

    private static final int CHOOSE_FILE_CODE = 0;

    /*select self cert*/
    public void fileChooser(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/pem");
        try {
            startActivityForResult(intent, CHOOSE_FILE_CODE);
        } catch (ActivityNotFoundException e) {
           // Toast.makeText(this, "亲，木有文件管理器啊-_-!!", Toast.LENGTH_SHORT).show();
           // Toast.makeText(this,"ddd",Toast.LENGTH_SHORT).show();
            Log.d("tag","木有文件管理器");
        }
    }
    /*select file back*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        //通过data.getData()方法返回的是Uri


           String x= getPath(data.getData());//.getPath();
         Log.d("tag","path:"+x);

        /*
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CHOOSE_FILE_CODE) {
                Uri uri = data.getData();
            }
        } else {
            Log.e(TAG1, "onActivityResult() error, resultCode: " + resultCode);
        }*/

        findPreference("caFile").setSummary(x);

        findPreference("caFile").getEditor().putString("caFile",x).commit();

    }


    public String getPath(Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }




    public void setPreferenceEnabled(boolean enabled) {
        for (String name : PROXY_PREFS) {
            Preference pref = findPreference(name);
            if (pref != null) {
                pref.setEnabled(enabled);
            }
        }
        for (String name : FEATURE_PREFS) {
            Preference pref = findPreference(name);
            if (pref != null) {
                if (name.equals(Constants.Key.isGlobalProxy) || name.equals(Constants.Key.proxyedApps)) {
                    pref.setEnabled(enabled && (Utils.isLollipopOrAbove()));

                } else {
                    pref.setEnabled(enabled);
                }
            }
        }
    }

    public void updatePreferenceScreen(Profile profile) {
        for (String name : PROXY_PREFS) {
            Preference pref = findPreference(name);
            if (pref != null) {
                updatePreference(pref, name, profile);
            }
        }
        for (String name : FEATURE_PREFS) {
            Preference pref = findPreference(name);
            if (pref != null) {
                updatePreference(pref, name, profile);
            }
        }
    }

    private void updateListPreference(Preference pref, String value) {
        ((ListPreference)pref).setValue(value);
    }

    private void updatePasswordEditTextPreference(Preference pref, String value) {
        pref.setSummary(value);
        ((PasswordEditTextPreference)pref).setText(value);
    }

    private void updateSummaryEditTextPreference(Preference pref, String value) {
        pref.setSummary(value);
        ((SummaryEditTextPreference)pref).setText(value);
    }

    private void updateProfileEditTextPreference(Preference pref, String value) {
        ((ProfileEditTextPreference)pref).resetSummary(value);
        ((ProfileEditTextPreference)pref).setText(value);
    }

    private void updateCheckBoxPreference(Preference pref, boolean value) {
        ((CheckBoxPreference)pref).setChecked(value);
    }

    public void updatePreference(Preference pref, String name, Profile profile) {
        switch (name) {
            case Constants.Key.profileName: updateProfileEditTextPreference(pref, profile.getName()); break;
            case Constants.Key.proxy: updateSummaryEditTextPreference(pref, profile.getProxy()); break;
            case Constants.Key.protocol:updateListPreference(pref, profile.getProtocol());break;
            case Constants.Key.remotePort: updateSummaryEditTextPreference(pref, Integer.toString(profile.getRemotePort())); break;
            case Constants.Key.localPort: updateSummaryEditTextPreference(pref, Integer.toString(profile.getLocalPort())); break;
            case Constants.Key.sitekey: updatePasswordEditTextPreference(pref, profile.getPassword()); break;
            case Constants.Key.route: updateListPreference(pref, profile.getRoute()); break;
            case Constants.Key.isGlobalProxy: updateCheckBoxPreference(pref, profile.isGlobal()); break;
            case Constants.Key.isUdpDns: updateCheckBoxPreference(pref, profile.isUdpdns()); break;
            default: break;
        }
    }


}
