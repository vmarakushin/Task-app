package accounting_service.data.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "job")
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @EqualsAndHashCode.Include
    private Long taskId;

    private UUID workerId;

    private boolean paid;

    private boolean finished;
}
