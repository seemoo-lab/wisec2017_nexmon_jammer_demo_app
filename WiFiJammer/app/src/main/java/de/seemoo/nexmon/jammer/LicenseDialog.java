package de.seemoo.nexmon.jammer;

/**
 * Created by stathis on 5/11/17.
 */

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;


public class LicenseDialog extends DialogFragment {

    WebView wvLicense;

    public LicenseDialog() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LicenseDialog.
     */
    // TODO: Rename and change types and number of parameters
    public static LicenseDialog newInstance() {
        LicenseDialog fragment = new LicenseDialog();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_license_dialog, container, false);

        getDialog().setTitle("Licenses");

        wvLicense = (WebView) view.findViewById(R.id.wvLicense);

        wvLicense.loadUrl("file:///android_asset/html/licenses.html");

        return view;
    }


}