package accounting_service.service;

import accounting_service.data.dto.BalanceResponse;
import accounting_service.data.model.Balance;
import accounting_service.exception.NotFoundException;
import accounting_service.repository.BalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final BalanceRepository balanceRepository;

    private final String NOT_FOUND = "Balance with id %s was not found";

    @Transactional(readOnly = true)
    public Page<BalanceResponse> getPage(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return balanceRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public BalanceResponse findById(UUID id) {
        return balanceRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new NotFoundException(
                        NOT_FOUND.format(id.toString())
                ));
    }

    private BalanceResponse mapToResponse(Balance balance) {
        return new BalanceResponse(balance.getWorkerId(), balance.getAmount());
    }
}
