package truong.nv.clockos.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object MainRouter

@Serializable
data class TaskDetailRouter(val taskId: Long?)