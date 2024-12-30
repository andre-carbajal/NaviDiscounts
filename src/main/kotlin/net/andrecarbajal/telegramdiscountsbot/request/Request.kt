package net.andrecarbajal.telegramdiscountsbot.request

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Entity
@Table(name = "request")
data class Request(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
    @JoinColumn(name = "chat_id") val chatId: Long = 0,
    val url: String = " ",
    @JoinColumn(name = "postpone_date") var postponeDate : LocalDate? = null
)

@EnableJpaRepositories
@Repository
interface RequestRepository : JpaRepository<Request, Long> {
    fun findByChatIdAndUrl(chatId: Long, url: String): Request?
    fun findAllByChatId(chatId: Long): List<Request>
}
