package ch.ethz.inf.vs.a1.gmtui.ble;

import android.graphics.Color;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * Created by Philippe on 09.10.16.
 */

public class GraphContainerImpl implements GraphContainer {
    GraphView graphView;
    int numberValues;
    private LineGraphSeries<DataPoint>[] series;

    //Constructor
    public GraphContainerImpl (GraphView graph, int numberValuess, float range){
        graphView = graph;
        numberValues = numberValuess;
        series = new LineGraphSeries[numberValues];

        for(int i = 0 ; i < numberValues; i++){

            series[i] = new LineGraphSeries<>();
            switch (i){
                case 0: series[i].setColor(Color.RED); break;
                case 1: series[i].setColor(Color.BLUE); break;
                case 2: series[i].setColor(Color.GREEN); break;
                case 3: series[i].setColor(Color.CYAN); break;
                case 4: series[i].setColor(Color.MAGENTA); break;
                default: series[i].setColor(Color.DKGRAY); break;
            }
            graphView.addSeries(series[i]);
        }

        //customize
        Viewport viewport = graphView.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(- range);
        viewport.setMaxY(range);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
    }

    public void addValues(double xIndex, float[] values){
        //display only 100 points on the viewport
        for(int i = 0; i < numberValues; i++) {
            series[i].appendData(new DataPoint(xIndex, values[i]), false, 100);

        }
        graphView.getViewport().setMaxX(series[0].getHighestValueX());
        graphView.getViewport().setMinX(series[0].getLowestValueX());
    };

    /**
     * Get all values currently displayed in the graph.
     *
     * @return A matrix containing the values in the right order (oldest values first).
     *         The rows are the series, the columns the values for each series.
     */
    public float[][] getValues(){
    return null;
    };
}
