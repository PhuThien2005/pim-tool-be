package vn.elca.training.model.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Setter
@Getter
@MappedSuperclass
public abstract class AbstractBaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "ID", nullable = false, updatable = false, precision = 19, scale = 0)
    private Long id;

    @Version
    @Column(name = "VERSION", precision = 10, scale = 0, nullable = false)
    private Long version;
}
