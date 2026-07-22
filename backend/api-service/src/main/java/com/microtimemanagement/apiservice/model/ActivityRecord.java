package com.microtimemanagement.apiservice.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mtm_activity_record")
@EqualsAndHashCode(callSuper = true)
public class ActivityRecord extends BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String recordDate;

    // Unidirectional owning @OneToMany — the service mutates this list in place
    // (chronological insert / remove) and saves the record; orphanRemoval keeps
    // deletes propagating. Eager so the record's activities render outside a tx.
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "activity_record_id")
    @OrderBy("startTimeEpoch ASC")
    private List<Activity> activities;

    private String createdBy;

}
