package de.seemoo.nexmon.jammer;

import android.graphics.Color;
import android.os.Bundle;
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
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;


/**
 * Created by Stathis on 03-May-17.
 */

public class PlotFragment extends android.app.Fragment {
    public int mode;
    public double[] amps;
    public double[] phases;
    public double[] freqs;
    public float[] times;
    public ArrayList<double[]> data = new ArrayList<>();
    public float[] timeI;
    public float[] timeQ;


    private LineChart mChart;
    private ILineDataSet set_real;
    private ILineDataSet set_imag;
    private ILineDataSet set_freq;

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

        timeI = new float[idft_size];
        timeQ = new float[idft_size];
        times = new float[idft_size];

        for (int n = 0; n < idft_size; n++) {
            timeI[n] = 0.0f;
            timeQ[n] = 0.0f;
        }

        for (int k = 0; k < amps.length; k++) {
            for (int n = 0; n < idft_size; n++) {
                times[n] = (float) (n / fs);
                timeI[n] -= amps[k] * Math.sin(2*Math.PI*freqs[k]*times[n] + phases[k]);
                timeQ[n] += amps[k] * Math.cos(2*Math.PI*freqs[k]*times[n] + phases[k]);
            }
        }

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
        }

    }

    public void createTimePlot() {

        TextView title = (TextView) getView().findViewById(R.id.plot_title);
        title.setText("Time Domain Plot");
        title.setBackgroundColor(Color.LTGRAY);

        mChart = (LineChart) getView().findViewById(R.id.chart1);


        // disable description text
        mChart.getDescription().setEnabled(false);

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
        l.setEnabled(true);
        l.setForm(Legend.LegendForm.LINE);


        XAxis xl = mChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawAxisLine(true);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        xl.setValueFormatter(new SmallValueFormatter("s"));


        YAxis leftAxis = mChart.getAxisLeft();

        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);


        updateTimePlot();
    }

    public void createFreqPlot() {
        TextView title = (TextView) getView().findViewById(R.id.plot_title);
        title.setText("Frequency Domain Plot");
        title.setBackgroundColor(Color.LTGRAY);

        mChart = (LineChart) getView().findViewById(R.id.chart1);


        // disable description text
        mChart.getDescription().setEnabled(false);

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
        l.setEnabled(true);
        l.setForm(Legend.LegendForm.LINE);


        XAxis xl = mChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawAxisLine(true);
        xl.setTextColor(Color.BLACK);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);
        xl.setValueFormatter(new LargeValueFormatter("Hz"));


        YAxis leftAxis = mChart.getAxisLeft();

        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(true);
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);


        updateFreqPlot();
    }

    public void updateTimePlot() {

        mChart.clearValues();

        LineData data = mChart.getData();

        set_real = createSet(ColorTemplate.rgb("#75c4f2"), "I");
        set_imag = createSet(ColorTemplate.rgb("#efef47"), "Q");
        data.addDataSet(set_real);
        data.addDataSet(set_imag);


        constructIQSamples();


        for (int i = 0; i < times.length; i++) {


            data.addEntry(new Entry(times[i], timeI[i]), 0);
            data.addEntry(new Entry(times[i], timeQ[i]), 1);

        }


        data.notifyDataChanged();

        // let the chart know it's data has changed
        mChart.notifyDataSetChanged();
        mChart.fitScreen();
    }

    public void updateFreqPlot() {

        mChart.clearValues();

        LineData data = mChart.getData();

        set_freq = createSet(ColorTemplate.rgb("#75c4f2"), "Frequencies");
        data.addDataSet(set_freq);


        for (int i = 0; i < freqs.length; i++) {
            data.addEntry(new Entry((float) freqs[i], (float) amps[i]), 0);
        }


        data.notifyDataChanged();

        // let the chart know it's data has changed
        mChart.notifyDataSetChanged();
        mChart.fitScreen();

    }

    private LineDataSet createSet(int color, String legend) {
        LineDataSet set = new LineDataSet(null, legend);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setDrawFilled(false);
        set.setDrawCircles(false);
        set.setColor(color);
        set.setHighlightEnabled(false);
        set.setDrawVerticalHighlightIndicator(false);
        set.setDrawHorizontalHighlightIndicator(false);
        set.setDrawValues(false);
        return set;
    }


}
