package vn.elca.training.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class HealthControllerTest {

    private MockMvc mockMvc;
    private HealthController healthController;

    @Before
    public void setUp() {
        healthController = new HealthController();
        mockMvc = MockMvcBuilders.standaloneSetup(healthController).build();
    }

    @Test
    public void testPing_WithMockMvc_ReturnsPong() throws Exception {
        mockMvc.perform(get("/ping").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    @Test
    public void testPing_DirectInvocation_ReturnsPong() {
        ResponseEntity<String> response = healthController.ping();
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatusCodeValue());
        Assert.assertEquals("pong", response.getBody());
    }
}
