package com.example.bookreader.data.models

class SharedData {
    companion object {
        // 0 for last read ,  1 for recent, 2 for recommended, 3 for search results
        var RecommendedBooks = mutableListOf<LocalBook>()
        var RecentBooks = mutableListOf<LocalBook>()
        var lastReadBook = LocalBook(id = "")
        var searchResults = mutableListOf<LocalBook>()
        var currentlyDownloadingBooks = mutableListOf<LocalBook>()
        var FavoritedList = mutableListOf<LocalBook>()
        var DownloadedList = mutableListOf<LocalBook>()
    }
}