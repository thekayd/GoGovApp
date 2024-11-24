// ReportOperations.kt
package com.kayodedaniel.gogovmobile.interfaces

import com.kayodedaniel.gogovmobile.data.ReportData

// interface class for submitting and validating reports
interface ReportOperations {
    suspend fun submitReport(reportData: ReportData)
    fun validateReport(reportData: ReportData): Boolean
}