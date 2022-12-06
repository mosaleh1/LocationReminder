package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val data: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

//DONE    TODO: Create a fake data source to act as a double to the real data source

    var shouldReturnError = false

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (data.isEmpty() || shouldReturnError) {
            throw Exception("Array is Empty")
            Result.Success(data)
        } else {
            Result.Error("No Reminders")
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        data.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Not found")
        }
        return try {
            val newList = data.filter {
                it.id == id
            }
            if (newList.isEmpty()) {
                throw Exception("Not found")
            }
            Result.Success(newList[0])
        } catch (e: Exception) {
            Result.Error("Reminder not found!")
            Result.Error(e.localizedMessage)
        }

    }

    override suspend fun deleteAllReminders() {
        data.clear()
    }


}