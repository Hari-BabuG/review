package com.zuzu.review.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "hotels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Hotel {
  @Id
  private Long id; // matches provider payload hotelId

  private String name;
}
