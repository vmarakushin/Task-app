package task_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import task_service.data.model.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}
