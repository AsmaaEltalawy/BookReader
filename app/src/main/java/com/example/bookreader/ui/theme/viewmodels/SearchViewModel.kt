package com.example.bookreader.ui.theme.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bookreader.data.local.AppDatabase
import com.example.bookreader.data.models.RecentSearches
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel(val app: Application) : AndroidViewModel(app){
    private val _recentSearches = MutableLiveData<List<RecentSearches>>()
    val recentSearches: MutableLiveData<List<RecentSearches>> = _recentSearches

    fun getRecentSearches(limit: Int){
        val searchDao = AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).searchesDao()
        viewModelScope.launch(Dispatchers.IO){
            _recentSearches.postValue(searchDao.getRecentSearches(limit))
        }
    }

    fun addQuery(query: String){
        val searchDao = AppDatabase.DatabaseBuilder.getInstance(app.applicationContext).searchesDao()
        viewModelScope.launch(Dispatchers.IO){
            searchDao.addQuery(RecentSearches(query = query))
        }

    }

}