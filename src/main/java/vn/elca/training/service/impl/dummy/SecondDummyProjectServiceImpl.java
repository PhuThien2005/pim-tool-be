package vn.elca.training.service.impl.dummy;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.elca.training.service.ProjectService;

/**
 * @author gtn
 *
 */
@Service("secondDummyProjectServiceImpl")
@Profile("dummy")
public class SecondDummyProjectServiceImpl extends AbstractDummyProjectService implements ProjectService {
}
