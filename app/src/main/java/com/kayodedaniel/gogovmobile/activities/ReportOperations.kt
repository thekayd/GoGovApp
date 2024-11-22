package com.kayodedaniel.gogovmobile.activities

import com.kayodedaniel.gogovmobile.data.ReportData

// Base interface for report operations (Polymorphism)
interface ReportOperations {
    suspend fun submitReport(reportData: ReportData)
    fun validateReport(reportData: ReportData): Boolean
}