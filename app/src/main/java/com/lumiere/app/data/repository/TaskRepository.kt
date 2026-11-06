package com.lumiere.app.data.repository

import com.lumiere.app.data.local.dao.*
import com.lumiere.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val subtaskDao: SubtaskDao,
    private val projectDao: ProjectDao,
    private val taskHistoryDao: TaskHistoryDao
) {
    val allTasks: Flow<List<Task>> = taskDao.getAllActiveTasks()
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()

    suspend fun insertTask(task: Task): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    fun getSubtasks(taskId: Long) = subtaskDao.getSubtasksForTask(taskId)
    suspend fun insertSubtask(subtask: Subtask) = subtaskDao.insertSubtask(subtask)
    suspend fun updateSubtask(subtask: Subtask) = subtaskDao.updateSubtask(subtask)
    suspend fun deleteSubtask(subtask: Subtask) = subtaskDao.deleteSubtask(subtask)

    suspend fun insertProject(project: Project) = projectDao.insertProject(project)

    fun getHistory(taskId: Long) = taskHistoryDao.getHistoryForTask(taskId)
    suspend fun insertHistory(history: TaskHistory) = taskHistoryDao.insertHistory(history)
}
