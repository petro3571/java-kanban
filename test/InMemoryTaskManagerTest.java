import manager.Managers;
import org.junit.jupiter.api.BeforeEach;

class InMemoryTaskManagerTest extends AbstractClassTMTest {
    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefaultTaskManager();
    }
}