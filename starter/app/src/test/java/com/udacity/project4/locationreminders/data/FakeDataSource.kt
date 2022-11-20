package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val data: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

//DONE    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return Result.Success(data)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        data.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val newList = data.filter {
            it.id == id
        }
        return when (newList.isEmpty()) {
            true -> Result.Success(newList[0])
            false -> Result.Error("not found")
        }
    }

    override suspend fun deleteAllReminders() {
        data.clear()
    }


}