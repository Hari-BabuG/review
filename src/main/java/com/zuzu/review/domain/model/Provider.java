package com.zuzu.review.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "providers")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Provider {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(nullable = false, unique = true)
  private String name;
}
