package com.example.data

import kotlinx.coroutines.flow.Flow

class AdmissionRepository(private val admissionDao: AdmissionDao) {
    val allAdmissions: Flow<List<AdmissionRecord>> = admissionDao.getAllAdmissions()

    suspend fun getAdmissionById(id: Long): AdmissionRecord? {
        return admissionDao.getAdmissionById(id)
    }

    suspend fun insert(record: AdmissionRecord): Long {
        return admissionDao.insertAdmission(record)
    }

    suspend fun delete(record: AdmissionRecord) {
        admissionDao.deleteAdmission(record)
    }

    suspend fun deleteById(id: Long) {
        admissionDao.deleteAdmissionById(id)
    }
}
