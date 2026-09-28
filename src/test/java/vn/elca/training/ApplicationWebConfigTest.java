package vn.elca.training;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import vn.elca.training.model.dto.response.ProjectListResponse;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.ProjectStatus;

import java.time.LocalDate;
import java.util.Locale;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationWebConfigTest {

    @Autowired
    private ApplicationWebConfig applicationWebConfig;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private LocalValidatorFactoryBean validator;

    @Test
    public void testBeansNotNull() {
        Assert.assertNotNull(applicationWebConfig);
        Assert.assertNotNull(modelMapper);
        Assert.assertNotNull(messageSource);
        Assert.assertNotNull(validator);
    }

    @Test
    public void testH2ServletRegistration() {
        ServletRegistrationBean registrationBean = applicationWebConfig.h2servletRegistration();
        Assert.assertNotNull(registrationBean);
        Assert.assertTrue(registrationBean.getUrlMappings().contains("/h2console/*"));
    }

    @Autowired
    private vn.elca.training.config.CorsConfig corsConfig;

    @Test
    public void testCorsConfig() {
        Assert.assertNotNull(corsConfig);
        CorsRegistry registry = new CorsRegistry();
        corsConfig.addCorsMappings(registry);
        // Does not throw exception
    }

    @Test
    public void testModelMapper_CustomConverter() {
        // Test mapping valid Project to ProjectListResponse
        Project project = Project.builder()
                .projectNumber(1001)
                .name("Project Name")
                .customer("Customer Name")
                .status(ProjectStatus.NEW)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();
        project.setId(10L);

        ProjectListResponse response = modelMapper.map(project, ProjectListResponse.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(Long.valueOf(10L), response.getId());
        Assert.assertEquals(Integer.valueOf(1001), response.getProjectNumber());
        Assert.assertEquals("Project Name", response.getName());
        Assert.assertEquals("Customer Name", response.getCustomer());
        Assert.assertEquals(ProjectStatus.NEW, response.getStatus());

        // Test converter directly with null source to cover branch
        org.modelmapper.spi.MappingContext<Project, ProjectListResponse> mockContext = org.mockito.Mockito.mock(org.modelmapper.spi.MappingContext.class);
        org.modelmapper.Converter<Project, ProjectListResponse> converter = modelMapper.getTypeMap(Project.class, ProjectListResponse.class).getConverter();
        Assert.assertNotNull(converter);
        org.mockito.Mockito.when(mockContext.getSource()).thenReturn(null);
        Assert.assertNull(converter.convert(mockContext));
    }

    @Test
    public void testMessageSource_ResolveMessage() {
        String msg = messageSource.getMessage("project.number.already.exists", null, "Default", Locale.ENGLISH);
        Assert.assertNotNull(msg);
    }

    @Test
    public void testConfigureSpringApplicationBuilder() {
        SpringApplicationBuilder builder = new SpringApplicationBuilder();
        SpringApplicationBuilder configured = applicationWebConfig.configure(builder);
        Assert.assertNotNull(configured);
    }

    @Test
    public void testApplicationLauncherInstantiation() {
        ApplicationLauncher launcher = new ApplicationLauncher();
        Assert.assertNotNull(launcher);
    }
}
