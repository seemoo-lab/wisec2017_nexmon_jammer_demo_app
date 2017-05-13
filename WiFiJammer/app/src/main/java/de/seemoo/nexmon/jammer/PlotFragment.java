package de.seemoo.nexmon.jammer;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by Stathis on 03-May-17.
 */

public class PlotFragment extends android.app.Fragment {
    public int mode;
    public double[] amps;
    public double[] phases;
    public double[] time_i;
    public double[] time_q;
    public double[] freqs;
    public double[] times;
    public ArrayList<double[]> data = new ArrayList<>();
    public double[] timeI;
    public double[] timeQ;


    private LineChart mChart;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            //Restore the fragment's state here
            amps = savedInstanceState.getDoubleArray("amps");
            phases = savedInstanceState.getDoubleArray("phases");
            freqs = savedInstanceState.getDoubleArray("freqs");
            mode = savedInstanceState.getInt("mode");
        } else {
            amps = getArguments().getDoubleArray("amps");
            phases = getArguments().getDoubleArray("phases");
            freqs = getArguments().getDoubleArray("freqs");
            mode = getArguments().getInt("mode");
        }
        /**
         * Inflate the layout for this fragment
         */
        if (mode == 0) return inflater.inflate(R.layout.time_plot_fragment, container, false);
        else return inflater.inflate(R.layout.freq_plot_fragment, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        createTimePlot();
        if (mode == 0) createTimePlot();
        else createFreqPlot();

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    public void constructIQSamples() {

        // TODO update idft_size according to setting
        int idft_size = 128;

        // TODO update bandwidth according to settings
        double bandwidth = 20e6;
        double fs = bandwidth * Constants.OVERSAMPLING_RATE;

        timeI = new double[idft_size];
        timeQ = new double[idft_size];
        times = new double[idft_size];

        for (int n = 0; n < idft_size; n++) {
            timeI[n] = 0.0;
            timeQ[n] = 0.0;
        }

        for (int k = 0; k < amps.length; k++) {
            for (int n = 0; n < idft_size; n++) {
                times[n] = n / fs;
                timeI[n] -= amps[k] * Math.sin(2*Math.PI*freqs[k]*times[n] + phases[k]);
                timeQ[n] += amps[k] * Math.cos(2*Math.PI*freqs[k]*times[n] + phases[k]);
            }
        }

    }
    public void constructFFTPlotData() {

        int size = amps.length;
        Double values[] = new Double[size];

        Log.d("D", "size: " + size);

        for (int i = 0; i < size; i++) {
            //values[i] = (size / 2 - i) * (-1);
            values[i] = freqs[i];
        }
        System.out.println(Arrays.toString(values));

        Double mags[] = new Double[size];

        for (int i = 0; i < size; i++) {
            mags[i] = amps[i];
        }

        List<? extends Number> xVals = Arrays.asList(values);
        List<? extends Number> yVals = Arrays.asList(mags);


    }

    public void plotSignals(double[] amps_new, double[] phases_new, double[] freqs_new) {
        this.amps = amps_new;
        this.phases = phases_new;
        this.freqs = freqs_new;


        if (mode == 0) {
            // Time Plot
            updateTimePlot();
        } else {
            // Frequency Plot
            updateFreqPlot();
            //constructFFTPlotData();
        }

    }

    public void createTimePlot() {

        TextView title = (TextView) getView().findViewById(R.id.plot_title);
        title.setText("Time Domain Plot");

        mChart = (LineChart) getView().findViewById(R.id.chart1);


        // enable description text
        mChart.getDescription().setEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        //l.setTypeface(mTfLight);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        //xl.setTypeface(mTfLight);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        //leftAxis.setTypeface(mTfLight);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(0.04f);
        leftAxis.setAxisMinimum(-0.04f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        updateTimePlot();
    }

    public void createFreqPlot() {
        TextView title = (TextView) getView().findViewById(R.id.plot_title);
        title.setText("Frequency Domain Plot");
    }

    public void updateTimePlot() {
        mChart.clearValues();

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set_real = data.getDataSetByIndex(0);
            ILineDataSet set_imag = data.getDataSetByIndex(1);
            // set.addEntry(...); // can be called as well

            if (set_real == null) {
                set_real = createSet();
                data.addDataSet(set_real);
                //set_imag = createSet();
                //data.addDataSet(set_imag);
            }

            constructIQSamples();


            double[] real = timeI;
            double[] imag = timeQ;

            //System.out.println(Arrays.toString(real));
            //System.out.println(Arrays.toString(imag));


            for (int i = 0; i < times.length; i++) {


                data.addEntry(new Entry((float) times[i], (float) real[i]), 0);
                //data.addEntry(new Entry(times[i], imag[i]), 1);

            }


            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();


        }

    }

    public void updateFreqPlot() {

    }

    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

}
