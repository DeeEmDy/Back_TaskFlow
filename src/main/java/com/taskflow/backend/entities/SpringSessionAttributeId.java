package com.taskflow.backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@Embeddable
public class SpringSessionAttributeId implements java.io.Serializable {
    private static final long serialVersionUID = 8140206102189785690L;
    @Column(name = "session_primary_id", nullable = false)
    private String sessionPrimaryId;

    @Column(name = "attribute_name", nullable = false)
    private String attributeName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        SpringSessionAttributeId entity = (SpringSessionAttributeId) o;
        return Objects.equals(this.sessionPrimaryId, entity.sessionPrimaryId) &&
                Objects.equals(this.attributeName, entity.attributeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionPrimaryId, attributeName);
    }

}