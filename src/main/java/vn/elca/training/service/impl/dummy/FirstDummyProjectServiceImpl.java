package vn.elca.training.service.impl.dummy;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import vn.elca.training.service.ProjectService;

/**
 * @author gtn
 *
 */
@Component("firstDummyProjectServiceImpl")
@Profile("dummy")
@Primary
public class FirstDummyProjectServiceImpl extends AbstractDummyProjectService implements ProjectService {
}
