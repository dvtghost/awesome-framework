package io.awesome.model;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public interface BaseModel<ID> {
    ID getId();
}
