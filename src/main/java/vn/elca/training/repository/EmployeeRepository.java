package vn.elca.training.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.elca.training.model.entity.Employee;

import java.util.Collection;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Slice<Employee> findByVisaStartingWithIgnoreCaseOrFirstNameStartingWithIgnoreCaseOrLastNameStartingWithIgnoreCase(
            String visa, String firstName, String lastName, Pageable pageable);

    List<Employee> findByVisaIn(Collection<String> visas);
}
