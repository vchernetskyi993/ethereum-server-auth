package com.example.plugins

import io.ktor.server.application.Application
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.Entity
import org.ktorm.entity.add
import org.ktorm.entity.removeIf
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.timestamp
import org.ktorm.schema.varchar
import java.io.File
import java.time.Instant

fun Application.configureDatabase(): Database {
    val database = Database.connect(
        url = environment.config.property("app.db.url").getString(),
        driver = environment.config.property("app.db.driver").getString(),
    )

    migrate(database)

    return database
}

private fun migrate(database: Database) {
    val migrationVersion = database.useConnection {
        it.prepareStatement("pragma user_version;").executeQuery().getInt("user_version")
    }
    val migrationPattern = Regex("^v(\\d+).+")

    File(
        (Application::class.java.getResource("/db")
            ?: throw IllegalStateException("Migrations folder not found. Expected to be '/db' resource."))
            .toURI()
    ).walk()
        .filter { it.isFile }
        .sortedBy { file ->
            migrationPattern.find(file.name)?.let { it.groupValues[1].toInt() }
                ?: throw IllegalArgumentException("Invalid migration file name: ${file.name}")
        }
        .withIndex()
        .drop(migrationVersion)
        .map { (i, f) -> IndexedValue(i + 1, f.readText()) }
        .forEach { (i, migration) ->
            database.useTransaction { tx ->
                val conn = tx.connection
                conn.prepareStatement(migration).execute()
                conn.prepareStatement("PRAGMA user_version = $i;").execute()
                conn.commit()
            }
        }
}

interface Nonce : Entity<Nonce> {
    companion object : Entity.Factory<Nonce>()

    var address: String
    var nonce: String
    var issuedAt: Instant
}

object Nonces : Table<Nonce>("nonce") {
    val address = varchar("address").primaryKey().bindTo { it.address }
    val nonce = varchar("nonce").primaryKey().bindTo { it.nonce }
    val issuedAt = timestamp("issued_at").bindTo { it.issuedAt }
}

class NonceRepository(val database: Database) {
    fun add(address: String, nonce: String) {
        database.sequenceOf(Nonces).add(nonce(address, nonce))
    }

    private fun nonce(a: String, n: String) = Nonce {
        address = a
        nonce = n
        issuedAt = Instant.now()
    }

    fun remove(address: String, nonce: String): Boolean =
        database.sequenceOf(Nonces)
            .removeIf { (it.address eq address) and (it.nonce eq nonce) } > 0

}


