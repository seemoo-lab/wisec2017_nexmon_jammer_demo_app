package de.seemoo.nexmon.jammer;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Date;

import static java.lang.Math.ceil;

/**
 * Created by Stathis on 04-Apr-17.
 */

public class SeekBarFragment extends android.app.Fragment {
    public double data[];
    public double freqs[];
    public String name;
    public int color;
    public int bandwidth;
    public LinearLayout container;
    public LayoutInflater inflater;
    public boolean started = false;

    // Interface to pass data to Activity
    Bundle bundle = new Bundle();
    FragmentListener mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (FragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void setData(double[] data) {
        this.data = data;
    }

    public void setFreqs(double[] freqs) {
        this.freqs = freqs;
    }

    public void setBandwidth(int band) {
        this.bandwidth = band;
        updateFrequencies();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         * Inflate the layout for this fragment
         */
        return inflater.inflate(R.layout.seekbar_fragment, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (data == null) {
            if (savedInstanceState != null) {
                //Restore the fragment's state here
                data = savedInstanceState.getDoubleArray("data");
                name = savedInstanceState.getString("name");
                color = savedInstanceState.getInt("color");
                bandwidth = savedInstanceState.getInt("Bandwidth");
                if (name == "Amplitudes") freqs = savedInstanceState.getDoubleArray("data");
            } else {
                name = getArguments().getString("name");
                data = getArguments().getDoubleArray(name);
                color = getArguments().getInt("color");
                bandwidth = getArguments().getInt("Bandwidth");
                if (name == "Amplitudes") freqs = getArguments().getDoubleArray("freqs");
            }
        }

        ((TextView) getView().findViewById(R.id.fragment_header)).setText(name);
        (getView().findViewById(R.id.fragment_header)).setBackgroundColor(color);

        inflater = LayoutInflater.from(getActivity());
        container = (LinearLayout) getView().findViewById(R.id.container);
        container.setBackgroundColor(color);

        setConcurrentScrolling(name);
        createSeekBars();
        setVerticalSeekBars();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putDoubleArray("data", data);
        outState.putString("name", name);
        outState.putInt("color", color);
        outState.putInt("Bandwidth", bandwidth);
    }

    public void setVerticalSeekBars() {
        final int sliders_count = data.length;
        for (int j = 0; j < Constants.MAX_SLIDERS_COUNT; j++) {
            LinearLayout layout = (LinearLayout) getView().getRootView().findViewWithTag(name + "_layout_" + j);
            if (j < sliders_count) layout.setVisibility(View.VISIBLE);
            else layout.setVisibility(View.GONE);
        }
        updateFrequencies();
        setScrollToMiddle();
        if (name.equals("Amplitudes")) passFreqs();
        bundle.putBoolean("setup", true);
        mCallback.onUserAction(bundle);

    }

    public void createSeekBars() {
        for (int i = 0; i < Constants.MAX_SLIDERS_COUNT; i++) {

            LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.verticalseekbar, container, false);
            layout.setTag(name + "_layout_" + i);
            //container.addView(layout);


            TextView freq = (TextView) layout.findViewById(R.id.verticalSeekbarFreq);

            freq.setTag(name + "_freq_" + i);

            TextView sliderText = (TextView) layout.findViewById(R.id.verticalSeekbarText);

            freq.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public synchronized boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        HorizontalScrollView scrollView = (HorizontalScrollView) getView().findViewById(R.id.horizontalScrollView);
                        // enable scrolling
                        scrollView.setOnTouchListener(null);
                        return true;
                    }
                    return false;
                }
            });

            sliderText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public synchronized boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        HorizontalScrollView scrollView = (HorizontalScrollView) getView().findViewById(R.id.horizontalScrollView);
                        // enable scrolling
                        scrollView.setOnTouchListener(null);
                        return true;
                    }
                    return false;
                }
            });

            sliderText.setTag(name + "_tag_" + i);


            final VerticalSeekBar verticalSeekBar = (VerticalSeekBar) layout.findViewById(R.id.verticalSeekbar);
            verticalSeekBar.setTag(name + "_seekBar_" + i);

            verticalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public synchronized void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    int tag = Integer.parseInt(((String) seekBar.getTag()).replaceAll("[^0-9]", ""));

                    TextView sliderText = (TextView) getView().findViewWithTag(name + "_tag_" + tag);
                    double value;
                    if (name == "Amplitudes") value = progress / 100.0;
                    else value = (progress - 50) / 100.0 * 2 * Math.PI;

                    value = ceil(value * 1000) / 1000.0;
                    sliderText.setText("" + value);
                    data[tag] = value;

                    // send new data to Activity
                    bundle.putDoubleArray(name, data);
                    mCallback.onUserAction(bundle);
                }
            });

            container.addView(layout);
        }
    }

    public void setConcurrentScrolling(String name) {
        HorizontalScrollView scrollView = (HorizontalScrollView) getView().findViewById(R.id.horizontalScrollView);
        scrollView.setTag(name);

        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if (v.getTag().toString().equals("Amplitudes")) {
                    HorizontalScrollView sv = (HorizontalScrollView) getView().getRootView().findViewWithTag("Phases");
                    sv.scrollTo(scrollX, scrollY);
                } else {
                    HorizontalScrollView sv = (HorizontalScrollView) getView().getRootView().findViewWithTag("Amplitudes");
                    sv.scrollTo(scrollX, scrollY);
                }

            }
        });

    }

    private double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return ceil(value * factor) / factor;
    }

    public void updateFrequencies() {
        int slidersCount = data.length;
        // TODO Make this idft_size depend on the real idft_size setting
        int idft_size = 128;

        double subcarrierSpacing = round((bandwidth * Constants.OVERSAMPLING_RATE / (double) idft_size) * 1000, 3);

        for (int j = 0; j < slidersCount; j++) {
            final LinearLayout layout = (LinearLayout) getView().getRootView().findViewWithTag(name + "_layout_" + j);
            final TextView freq = (TextView) getView().getRootView().findViewWithTag(name + "_freq_" + j);
            final TextView sliderText = (TextView) getView().getRootView().findViewWithTag(name + "_tag_" + j);

            double subcarrierFrequency = round(subcarrierSpacing * (slidersCount / 2 - j) * (-1), 4);

            if (Math.abs(subcarrierFrequency) <= bandwidth * 500) layout.setBackgroundColor(color);
            else layout.setBackgroundColor(Color.WHITE);

            if (name.equals("Amplitudes")) freqs[j] = subcarrierFrequency * 1000;

            final String freqUnit;
            if (Math.abs(subcarrierFrequency) >= 1000) {

                subcarrierFrequency = round(subcarrierFrequency / 1000, 3);

                freqUnit = "MHz";
            } else freqUnit = "kHz";

            int subcarrierNumber = j - slidersCount / 2;

            freq.setText("SC " + subcarrierNumber + " at\n" + subcarrierFrequency + freqUnit);

            if (name.equals("Amplitudes")) sliderText.setText("" + data[j]);
            else sliderText.setText("" + ceil(data[j] * 1000) / 1000.0);

            final VerticalSeekBar verticalSeekBar = (VerticalSeekBar) getView().getRootView().findViewWithTag(name + "_seekBar_" + j);

            Handler mHandler = new Handler();
            mHandler.post(new Runnable() {
                public void run() {
                    int tag = Integer.parseInt(((String) verticalSeekBar.getTag()).replaceAll("[^0-9]", ""));
                    double value;
                    if (name.equals("Amplitudes")) {
                        value = data[tag] * 100.0;
                    } else {
                        value = round((data[tag] * 2 * Math.PI) / 100 + 50, 3);
                    }
                    verticalSeekBar.setProgress((int) value);
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!started) {
            setScrollToMiddle();
            started = true;
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void setScrollToMiddle() {
        HorizontalScrollView scrollView = (HorizontalScrollView) getView().getRootView().findViewById(R.id.horizontalScrollView);

        int slidersCount = data.length;
        if (slidersCount % 2 != 0) slidersCount--;
        TextView freq = (TextView) getView().getRootView().findViewById(R.id.main).findViewWithTag(name + "_freq_" + 1);
        //System.out.println(x);
        //System.out.println(freq.getWidth());

        scrollView.scrollTo(freq.getWidth() * slidersCount / 2 - (scrollView.getWidth() / 2 - freq.getWidth() / 2), 0);
    }

    public void passFreqs() {
        bundle.putDoubleArray("freqs", freqs);
        mCallback.onUserAction(bundle);
    }

    public interface FragmentListener {
        void onUserAction(Bundle bundle);
    }
}
