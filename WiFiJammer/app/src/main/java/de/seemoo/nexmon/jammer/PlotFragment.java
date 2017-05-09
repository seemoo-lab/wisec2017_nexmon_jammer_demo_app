package de.seemoo.nexmon.jammer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * Created by Stathis on 03-May-17.
 */

public class PlotFragment extends android.app.Fragment {
    public double[] amps;
    public double[] phases;
    public double[] freqs;
    public Double[] times;
    public ArrayList<double[]> data = new ArrayList<>();
    Complex[] complexFrequencySignal;
    Complex[] complexTimeSignal;
    private XYPlot timePlot;
    private XYPlot freqPlot;
    private XYSeries series1;
    private XYSeries series2;
    private XYSeries series3;
    private LineAndPointFormatter seriesFormat1;
    private LineAndPointFormatter seriesFormat2;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         * Inflate the layout for this fragment
         */
        return inflater.inflate(R.layout.time_plot_fragment, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            //Restore the fragment's state here
            amps = savedInstanceState.getDoubleArray("amps");
            phases = savedInstanceState.getDoubleArray("phases");
            freqs = savedInstanceState.getDoubleArray("freqs");
        } else {
            amps = getArguments().getDoubleArray("amps");
            phases = getArguments().getDoubleArray("phases");
            freqs = getArguments().getDoubleArray("freqs");
        }

        timePlot = (XYPlot) getActivity().findViewById(R.id.timePlot);
        freqPlot = (XYPlot) getActivity().findViewById(R.id.freqPlot);

        seriesFormat1 = new LineAndPointFormatter(getActivity(), R.xml.line_point_formatter1);
        seriesFormat2 = new LineAndPointFormatter(getActivity(), R.xml.line_point_formatter2);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    public void constructIQSamples() {

        int fs = 40000000;
        int size = amps.length;

        times = new Double[size];

        for (int j = 0; j < size; j++) {
            times[j] = (j * 1.0) / fs;
        }
        System.out.println(Arrays.toString(times));

        double i;
        double q;

        complexFrequencySignal = new Complex[size];

        for (int j = 0; j < size; j++) {
            i = amps[j] * cos(phases[j]);
            q = (-1) * amps[j] * sin(phases[j]);
            complexFrequencySignal[j] = new Complex(i, q);
        }
        /*Complex[] x = new Complex[7];

        // original data
        for (int k = 0; k < 7; k++) {
            x[k] = new Complex(k+1, k+1);
        }
        Complex[] y = FFT.fftshift(x);
        FFT.show(y, "hi");*/

        complexTimeSignal = FFT.ifft(complexFrequencySignal);
    }

    public void constructFFTPlotData() {

        int size = amps.length;
        Integer values[] = new Integer[size];

        for (int i = 0; i < size; i++) {
            values[i] = (size / 2 - i) * (-1);
        }
        System.out.println(Arrays.toString(values));

        Complex x[] = FFT.fftshift(complexFrequencySignal);
        Double mags[] = new Double[size];

        for (int i = 0; i < size; i++) {
            mags[i] = x[i].abs();
        }

        List<? extends Number> xVals = Arrays.asList(values);
        List<? extends Number> yVals = Arrays.asList(mags);

        freqPlot.removeSeries(series3);
        series3 = new SimpleXYSeries(xVals, yVals, "FFT");
        freqPlot.addSeries(series3, seriesFormat1);
    }

    public void plotSignals(double[] amps_new, double[] phases_new, double[] freqs_new) {
        this.amps = amps_new;
        this.phases = phases_new;
        this.freqs = freqs_new;

        constructIQSamples();

        List<? extends Number> xVals = Arrays.asList(times);
        List<? extends Number> yValsReal = Arrays.asList(extractPart(complexTimeSignal, 0));
        List<? extends Number> yValsImag = Arrays.asList(extractPart(complexTimeSignal, 1));

        timePlot.removeSeries(series1);
        timePlot.removeSeries(series2);
        series1 = new SimpleXYSeries(xVals, yValsReal, "Real");
        series2 = new SimpleXYSeries(xVals, yValsImag, "Imaginary");
        timePlot.addSeries(series1, seriesFormat1);
        timePlot.addSeries(series2, seriesFormat2);

        constructFFTPlotData();
    }

    public Double[] extractPart(Complex[] complex, int part) {
        Double[] data = new Double[complex.length];
        if (part == 0) {
            //Real
            for (int j = 0; j < data.length; j++) {
                data[j] = complex[j].re();
            }
        } else {
            //Imaginary
            for (int j = 0; j < data.length; j++) {
                data[j] = complex[j].im();
            }
        }
        return data;
    }

}
