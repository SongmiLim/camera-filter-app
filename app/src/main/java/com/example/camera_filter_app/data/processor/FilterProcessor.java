package com.example.camera_filter_app.data.processor;

import android.util.Log;
import com.example.camera_filter_app.model.FilterType;

public class FilterProcessor {
    public void applyFilter(FilterType filterType) {
        switch (filterType) {
            case GRAYSCALE:
                Log.d("FilterProcessor", "Grayscale applied");
                break;
            case BRIGHTNESS:
                Log.d("FilterProcessor", "Brightness applied");
                break;
            case ORIGINAL:
                Log.d("FilterProcessor", "Original restored");
                break;
        }
    }

}
