package com.lumiere.app.data.repository

import com.lumiere.app.data.local.dao.*
import com.lumiere.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import com.lumiere.app.utils.LumiereLogger

class TaskRepository(
    private val taskDao: TaskDao,
    private val subtaskDao: SubtaskDao,
    private val projectDao: ProjectDao,
    private val taskHistoryDao: TaskHistoryDao
) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()

    suspend fun insertTask(task: Task): Result<Long> = runCatching {
        taskDao.insertTask(task)
    }.onFailure { if (it is kotlinx.coroutines.CancellationException) throw it; LumiereLogger.e("TaskRepo", "Falha ao inserir", it) }

    suspend fun updateTask(task: Task): Result<Unit> = runCatching {
        taskDao.updateTask(task)
    }.onFailure { if (it is kotlinx.coroutines.CancellationException) throw it; LumiereLogger.e("TaskRepo", "Falha ao atualizar", it) }

    suspend fun deleteTask(task: Task): Result<Unit> = runCatching {
        taskDao.deleteTask(task)
    }.onFailure { if (it is kotlinx.coroutines.CancellationException) throw it; LumiereLogger.e("TaskRepo", "Falha ao apagar", it) }

    suspend fun getTaskById(id: Long): Task? = taskDao.getTaskById(id)

    fun getSubtasks(taskId: Long) = subtaskDao.getSubtasksForTask(taskId)
    
    suspend fun insertSubtask(subtask: Subtask): Result<Long> = runCatching {
        subtaskDao.insertSubtask(subtask)
    }.onFailure { LumiereLogger.e("TaskRepo", "Falha ao inserir subtarefa", it) }

    suspend fun updateSubtask(subtask: Subtask): Result<Unit> = runCatching {
        subtaskDao.updateSubtask(subtask)
    }.onFailure { LumiereLogger.e("TaskRepo", "Falha ao atualizar subtarefa", it) }

    suspend fun deleteSubtask(subtask: Subtask): Result<Unit> = runCatching {
        subtaskDao.deleteSubtask(subtask)
    }.onFailure { LumiereLogger.e("TaskRepo", "Falha ao apagar subtarefa", it) }

    suspend fun insertProject(project: Project): Result<Long> = runCatching {
        projectDao.insertProject(project)
    }.onFailure { LumiereLogger.e("TaskRepo", "Falha ao inserir projeto", it) }

    fun getHistory(taskId: Long) = taskHistoryDao.getHistoryForTask(taskId)
    
    suspend fun insertHistory(history: TaskHistory): Result<Unit> = runCatching {
        taskHistoryDao.insertHistory(history)
    }.onFailure { LumiereLogger.e("TaskRepo", "Falha ao inserir histórico", it) }
}
