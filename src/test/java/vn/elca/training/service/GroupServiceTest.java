package vn.elca.training.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import vn.elca.training.model.dto.response.GroupListResponse;
import vn.elca.training.model.entity.Group;
import vn.elca.training.repository.GroupRepository;
import vn.elca.training.service.impl.GroupServiceImpl;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private GroupServiceImpl groupService;

    private Pageable pageable;
    private Group group;
    private GroupListResponse response;

    @Before
    public void setUp() {
        pageable = PageRequest.of(0, 10);

        group = new Group();
        group.setId(1L);

        response = GroupListResponse.builder()
                .id(1L)
                .build();
    }

    @Test
    public void testGetAll_ReturnsMappedSlice() {
        Slice<Group> groupSlice = new SliceImpl<>(Collections.singletonList(group), pageable, false);

        Mockito.when(groupRepository.findAllBy(pageable)).thenReturn(groupSlice);
        Mockito.when(modelMapper.map(group, GroupListResponse.class)).thenReturn(response);

        Slice<GroupListResponse> result = groupService.getAll(pageable);

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.getContent().size());
        Assert.assertEquals(Long.valueOf(1L), result.getContent().get(0).getId());

        Mockito.verify(groupRepository, Mockito.times(1)).findAllBy(pageable);
        Mockito.verify(modelMapper, Mockito.times(1)).map(group, GroupListResponse.class);
    }
}
