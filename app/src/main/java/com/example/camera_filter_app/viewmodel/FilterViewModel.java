package com.example.camera_filter_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.camera_filter_app.data.repository.FilterRepository;
import com.example.camera_filter_app.model.FilterType;

public class FilterViewModel extends ViewModel {
    private final FilterRepository repository = new FilterRepository();
    private final MutableLiveData<FilterType> currentFilter = new MutableLiveData<>(FilterType.ORIGINAL);

    public LiveData<FilterType> getCurrentFilter() {
        return currentFilter;
    }

    public void setFilter(FilterType type) {
        currentFilter.setValue(type);
        repository.apply(type);
    }

    public void resetFilter() {
        setFilter(FilterType.ORIGINAL);
    }
}
