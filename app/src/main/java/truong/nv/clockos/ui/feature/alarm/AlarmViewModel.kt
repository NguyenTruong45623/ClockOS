package truong.nv.clockos.ui.feature.alarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import truong.nv.clockos.data.dao.AlarmDao
import truong.nv.clockos.data.models.AlarmEntity
import truong.nv.clockos.helper.AlarmScheduler
import javax.inject.Inject

data class AlarmUiState(
    val alarms: List<AlarmEntity> = emptyList(),
    val isEditMode: Boolean = false
)

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val alarmDao: AlarmDao,
    application: Application
) : AndroidViewModel(application) {

    private val _isEditMode = MutableStateFlow(false)
    private val context = getApplication<Application>().applicationContext

    val uiState: StateFlow<AlarmUiState> = combine(
        alarmDao.getAllAlarms(),
        _isEditMode
    ) { alarms, isEditMode ->
        AlarmUiState(alarms = alarms, isEditMode = isEditMode)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AlarmUiState()
    )

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }

    fun setEditMode(editMode: Boolean) {
        _isEditMode.value = editMode
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
            alarmDao.updateAlarm(updatedAlarm)
            
            if (updatedAlarm.isEnabled) {
                AlarmScheduler.scheduleAlarm(context, updatedAlarm)
            } else {
                AlarmScheduler.cancelAlarm(context, updatedAlarm.id)
            }
        }
    }

    fun saveAlarm(
        id: Int = 0,
        hour: Int,
        minute: Int,
        repeatDays: List<Int>,
        label: String,
        isSnoozeEnabled: Boolean,
        snoozeDuration: Int,
        soundName: String
    ) {
        viewModelScope.launch {
            val alarm = AlarmEntity(
                id = id,
                hour = hour,
                minute = minute,
                repeatDays = repeatDays,
                label = label.ifBlank { "Báo thức" },
                isSnoozeEnabled = isSnoozeEnabled,
                snoozeDuration = snoozeDuration,
                soundName = soundName,
                isEnabled = true // Default to enabled when saved/updated
            )
            
            if (id == 0) {
                // New alarm
                val newId = alarmDao.insertAlarm(alarm).toInt()
                val savedAlarm = alarm.copy(id = newId)
                AlarmScheduler.scheduleAlarm(context, savedAlarm)
            } else {
                // Edit existing alarm
                alarmDao.updateAlarm(alarm)
                AlarmScheduler.scheduleAlarm(context, alarm)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(context, alarm.id)
            alarmDao.deleteAlarm(alarm)
        }
    }
}
