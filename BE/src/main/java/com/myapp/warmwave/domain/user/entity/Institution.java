package com.myapp.warmwave.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@AllArgsConstructor
@SuperBuilder
@DiscriminatorValue("institution")
public class Institution extends User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String institutionName;

    @Column(unique = true)
    private String registerNum;

    private Boolean isApprove;

    public void approve() {
        this.isApprove = true;
    }
}