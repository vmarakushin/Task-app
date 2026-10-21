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

    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank
    private String title;

    @Column(name = "worker_id")
    private UUID workerId;

    private String description;

    @Enumerated(EnumType.STRING)
    private Status status;
}
