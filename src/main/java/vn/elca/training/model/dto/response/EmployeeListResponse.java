package vn.elca.training.model.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeListResponse {
    private Long id;
    private Long version;
    private String visa;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
}