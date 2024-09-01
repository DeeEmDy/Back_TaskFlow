package com.taskflow.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "spring_session_attributes")
public class SpringSessionAttribute {
    @SequenceGenerator(name = "spring_session_attributes_id_gen", sequenceName = "tbuser_id_user_seq", allocationSize = 1)
    @EmbeddedId
    private SpringSessionAttributeId id;

    @Column(name = "attribute_bytes")
    private Long attributeBytes;

}