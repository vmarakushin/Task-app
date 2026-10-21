package search_service.data.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    /** id задачи из task-service: свой генератор в проекции не нужен. */
    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    private String title;

    /**
     * Просто id, без @ManyToOne: работник может ещё не прийти из user-stream.
     * Данные работника подтягиваются LEFT JOIN-ом в TaskRepository.
     */
    @Column(name = "worker_id")
    private UUID workerId;

    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;
}
