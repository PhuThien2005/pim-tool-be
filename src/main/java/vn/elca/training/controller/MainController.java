package vn.elca.training.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RestController;
import vn.elca.training.service.ProjectService;

/**
 * @author vlp
 *
 */
@RestController
public class MainController extends AbstractApplicationController {

    private ProjectService projectService;

    @Autowired
    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    @Value("${application.title}")
    private String title;

    @Value("${application.message}")
    private String message;

    @GetMapping("/main")
    public String main() {
        return title + ". " + String.format(message, projectService.count());
    }
}
