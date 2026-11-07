package com.lumiere.app.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lumiere.app.data.local.database.AppDatabase
import com.lumiere.app.data.repository.TaskRepository
import com.lumiere.app.shared.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TaskRepositoryIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: TaskRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        repository = TaskRepository(
            taskDao = db.taskDao(),
            subtaskDao = db.subtaskDao(),
            projectDao = db.projectDao(),
            taskHistoryDao = db.taskHistoryDao()
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertAndReadTask() = runTest {
        val task = TestDataFactory.task(title = "BD Real QA", status = 0)
        
        val id = repository.insertTask(task)
        assertTrue("O ID gerado pelo Room deve ser maior que 0", id > 0)

        val tasks = repository.allTasks.first()
        assertEquals(1, tasks.size)
        assertEquals("BD Real QA", tasks[0].title)
    }

    @Test
    fun allActiveTasks_excludesArchived() = runTest {
        val ativa = TestDataFactory.task(title = "Ativa", status = 0)
        val arquivada = TestDataFactory.task(title = "Arquivada", status = 4)

        repository.insertTask(ativa)
        repository.insertTask(arquivada)

        val resultado = repository.allTasks.first()
        
        assertEquals("Deveria retornar apenas 1 tarefa (a ativa)", 1, resultado.size)
        assertEquals("Ativa", resultado[0].title)
    }

    @Test
    fun updateTask_modifiesDatabase() = runTest {
        val task = TestDataFactory.task(title = "Original")
        val id = repository.insertTask(task)

        val updated = task.copy(id = id, title = "Modificada")
        repository.updateTask(updated)

        val tasks = repository.allTasks.first()
        assertEquals("Modificada", tasks[0].title)
    }
}
