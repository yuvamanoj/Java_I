package com.ibm.sec.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "terms_and_conditions")
@Getter
@Setter
@NoArgsConstructor
public class TermAndCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terms", nullable = false)
    private String terms;

    @Column(name = "version", nullable = false)
    private Integer version;

}
