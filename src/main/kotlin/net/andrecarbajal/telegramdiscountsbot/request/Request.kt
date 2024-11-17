package net.andrecarbajal.telegramdiscountsbot.request

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.stereotype.Repository

@Entity
@Table(name = "request")
data class Request(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
    @JoinColumn(name = "chat_id") val chatId: Long = 0,
    val url: String = " "
)

@EnableJpaRepositories
@Repository
interface RequestRepository : JpaRepository<Request, Long> {
    fun findByChatIdAndUrl(chatId: Long, url: String): Request?
    fun findAllByChatId(chatId: Long): List<Request>
}
