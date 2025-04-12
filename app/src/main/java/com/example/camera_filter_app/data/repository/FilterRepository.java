package com.example.camera_filter_app.data.repository;

import com.example.camera_filter_app.data.processor.FilterProcessor;
import com.example.camera_filter_app.model.FilterType;

public class FilterRepository {
    private final FilterProcessor processor = new FilterProcessor();

    public void apply(FilterType type) {
        processor.applyFilter(type);
    }
}
