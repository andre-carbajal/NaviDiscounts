package net.andrecarbajal.telegramdiscountsbot.request

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
class RequestRepositoryTest @Autowired constructor(
    private val requestRepository: RequestRepository
) {
    @Test
    fun saveAndFind_withLiquibaseManagedRequestTable_persistsRequest() {
        val request = requestRepository.save(
            Request(
                chatId = 123L,
                url = "https://www.mifarma.com.pe/producto/fotoprotector-isdin-fusion-water-magic-spf50/069106",
                postponeDate = LocalDate.of(2026, 6, 23)
            )
        )

        val savedRequests = requestRepository.findAllByChatId(123L)

        assertEquals(request.id, savedRequests.single().id)
        assertEquals(LocalDate.of(2026, 6, 23), savedRequests.single().postponeDate)
    }
}
