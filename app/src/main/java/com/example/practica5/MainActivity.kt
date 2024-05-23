package com.example.practica5

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.practica5.ui.theme.Practica5Theme

data class User(val name: String, val age: Int, val salary: Double)

class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this)

        // Insert initial users and update some records
        insertInitialUsers(dbHelper)
        updateUsers(dbHelper)

        setContent {
            Practica5Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val users = remember { mutableStateListOf<User>() }
                    users.addAll(dbHelper.getAllUsers())
                    UserTable(users)
                }
            }
        }
    }

    private fun insertInitialUsers(dbHelper: DatabaseHelper) {
        val dummyImage = ByteArray(0) // Example empty byte array for profile pictures
        dbHelper.insertUser("Alice", 30, 50000.0, dummyImage)
        dbHelper.insertUser("Bob", 25, 45000.0, dummyImage)
        dbHelper.insertUser("Charlie", 28, 55000.0, dummyImage)
        dbHelper.insertUser("Diana", 35, 60000.0, dummyImage)
        dbHelper.insertUser("Eve", 22, 40000.0, dummyImage)
    }

    private fun updateUsers(dbHelper: DatabaseHelper) {
        val newDummyImage = ByteArray(0) // Example updated empty byte array for profile pictures
        dbHelper.updateUser(1, "Alice Updated", 31, 51000.0, newDummyImage)
        dbHelper.updateUser(2, "Bob Updated", 26, 46000.0, newDummyImage)
    }
}

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "example.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "Users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_AGE = "age"
        private const val COLUMN_SALARY = "salary"
        private const val COLUMN_PROFILE_PICTURE = "profilePicture"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "$COLUMN_NAME TEXT,"
                + "$COLUMN_AGE INTEGER,"
                + "$COLUMN_SALARY REAL,"
                + "$COLUMN_PROFILE_PICTURE BLOB"
                + ")")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertUser(name: String, age: Int, salary: Double, profilePicture: ByteArray) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_AGE, age)
            put(COLUMN_SALARY, salary)
            put(COLUMN_PROFILE_PICTURE, profilePicture)
        }
        db.insert(TABLE_NAME, null, contentValues)
        db.close()
    }

    fun updateUser(id: Int, name: String?, age: Int?, salary: Double?, profilePicture: ByteArray?) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            name?.let { put(COLUMN_NAME, it) }
            age?.let { put(COLUMN_AGE, it) }
            salary?.let { put(COLUMN_SALARY, it) }
            profilePicture?.let { put(COLUMN_PROFILE_PICTURE, it) }
        }
        db.update(TABLE_NAME, contentValues, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT $COLUMN_NAME, $COLUMN_AGE, $COLUMN_SALARY FROM $TABLE_NAME", null)
        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE))
                val salary = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SALARY))
                users.add(User(name, age, salary))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return users
    }
}

@Composable
fun UserTable(users: List<User>) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(users) { user ->
            UserRow(user)
        }
    }
}

@Composable
fun UserRow(user: User) {
    Text(text = "Name: ${user.name}, Age: ${user.age}, Salary: ${user.salary}", modifier = Modifier.padding(8.dp))
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Practica5Theme {
        val users = listOf(
            User("Alice", 30, 50000.0),
            User("Bob", 25, 45000.0),
            User("Charlie", 28, 55000.0)
        )
        UserTable(users)
    }
}