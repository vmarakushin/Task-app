package accounting_service.integration;

import accounting_service.AbstractIntegrationTest;
import accounting_service.data.model.Balance;
import accounting_service.repository.BalanceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class BalanceControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BalanceRepository balanceRepository;

    private final UUID worker1uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final UUID worker2uuid = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final BigDecimal worker1amount = BigDecimal.valueOf(100);

    private final BigDecimal worker2amount = BigDecimal.valueOf(200);

    private Balance worker1balance;

    private Balance worker2balance;

    @BeforeEach
    void setUp() {

        // База общая для всех тестовых классов: начинаем с чистой таблицы,
        // не надеясь, что предыдущий класс убрал за собой
        balanceRepository.deleteAll();

        worker1balance = balanceRepository.save(
                Balance.builder()
                        .workerId(worker1uuid)
                        .amount(worker1amount)
                        .build()
        );

        worker2balance = balanceRepository.save(
                Balance.builder()
                        .workerId(worker2uuid)
                        .amount(worker2amount)
                        .build()
        );
    }

    @AfterEach
    void tearDown() {
        balanceRepository.deleteAll();
    }

    @Test
    @DisplayName("Получение задач с пагинацией")
    void getPage_shouldReturnPage() throws Exception {
        mockMvc.perform(
                        get("/api/balance")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                // Ищем свои записи по workerId, а не по позиции в списке:
                // позиция зависит от того, какие ещё строки есть в таблице
                .andExpect(jsonPath("$.content[?(@.workerId == '%s')].amount", worker1uuid)
                        .value(hasItem(worker1amount.doubleValue())))
                .andExpect(jsonPath("$.content[?(@.workerId == '%s')].amount", worker2uuid)
                        .value(hasItem(worker2amount.doubleValue())));
    }

    @Test
    @DisplayName("Получение задачи по id")
    void getById_shouldReturnTask() throws Exception {
        String template = String.format("/api/balance/%s", worker1balance.getWorkerId().toString());
        mockMvc.perform(
                        get(template)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workerId").value(worker1balance.getWorkerId().toString()))
                .andExpect(jsonPath("$.amount").value(worker1amount.longValue()));
    }

    @Test
    @DisplayName("Получение несуществующей задачи")
    void getById_shouldReturn404() throws Exception {
        mockMvc.perform(
                        get("/api/balance/33333333-3333-3333-3333-333333333333")
                )
                .andExpect(status().isNotFound());
    }
}
