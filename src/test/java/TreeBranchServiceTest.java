//import com.rexqwer.telegrambotassistant.config.ApplicationConfiguration;
//import com.rexqwer.telegrambotassistant.repository.MessageRequestRepository;
//import com.rexqwer.telegrambotassistant.repository.ScheduledTaskTypeRepository;
//import com.rexqwer.telegrambotassistant.service.dialog.DialogService;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.Profile;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//
//import javax.swing.tree.DefaultMutableTreeNode;
//
//@Slf4j
//@SpringBootTest(classes = ApplicationConfiguration.class)
//@Profile("dev")
//@ActiveProfiles("dev")
//@ExtendWith(SpringExtension.class)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//public class TreeBranchServiceTest {
//
//    private final DialogService treeBranchService;
//
//    public TreeBranchServiceTest(@Autowired DialogService treeBranchService) {
//        this.treeBranchService = treeBranchService;
//    }
//
//    @Test
//    @Order(1)
//    public void serviceWorks() {
//        DefaultMutableTreeNode defaultMutableTreeNode = treeBranchService.reminderTreeNode();
//        Assertions.assertNotNull(defaultMutableTreeNode);
//    }
//}
