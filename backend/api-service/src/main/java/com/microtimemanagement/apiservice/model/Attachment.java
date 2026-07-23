package com.microtimemanagement.apiservice.model;

import com.microtimemanagement.apiservice.enums.AttachmentOwnerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * A file (image or generic) attached to a parent entity, stored as a data-URL
 * base64 string (<= 5 MB). Scoped to the owning user via {@code uid}.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mtm_attachment")
@EqualsAndHashCode(callSuper = true)
public class Attachment extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    private AttachmentOwnerType parentType;

    private String parentId;

    private String name;

    private String contentType;

    private Long sizeBytes;

    @Column(columnDefinition = "text")
    private String dataBase64;

    /** Owning user's uid. */
    private String uid;
}
