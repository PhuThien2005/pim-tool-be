package vn.elca.training.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import vn.elca.training.model.dto.response.EmployeeListResponse;
import vn.elca.training.model.dto.response.GroupListResponse;
import vn.elca.training.service.GroupService;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupService groupService;

    @Test
    public void testGetAllGroups_DefaultParams_Returns200() throws Exception {
        EmployeeListResponse leader = EmployeeListResponse.builder()
                .id(1L)
                .visa("MDU")
                .firstName("Marc")
                .lastName("Dupond")
                .build();
        GroupListResponse group = GroupListResponse.builder()
                .id(1L)
                .groupLeader(leader)
                .build();
        Slice<GroupListResponse> slice = new SliceImpl<>(Collections.singletonList(group));

        Mockito.when(groupService.getAll(Mockito.any(Pageable.class))).thenReturn(slice);

        mockMvc.perform(get("/groups")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].groupLeader.visa").value("MDU"));
    }

    @Test
    public void testGetAllGroups_CustomPageable_Returns200() throws Exception {
        Slice<GroupListResponse> emptySlice = new SliceImpl<>(Collections.emptyList());
        Mockito.when(groupService.getAll(Mockito.any(Pageable.class))).thenReturn(emptySlice);

        mockMvc.perform(get("/groups")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
