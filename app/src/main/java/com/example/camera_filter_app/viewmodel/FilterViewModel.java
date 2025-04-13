package com.example.camera_filter_app.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.camera_filter_app.model.FilterType;

public class FilterViewModel extends ViewModel {
    private final MutableLiveData<FilterType> selectedFilter = new MutableLiveData<>(FilterType.ORIGINAL);
    public LiveData<FilterType> getSelectedFilter() {
        return selectedFilter;
    }

    public void setFilter(FilterType type) {
        selectedFilter.setValue(type);
    }
}
