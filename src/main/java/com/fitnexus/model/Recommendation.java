package com.fitnexus.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "recommendations")
@Data@NoArgsConstructor@AllArgsConstructor
@Builder
public class Recommendation {

    @Id
    private String id;
    private String activityId;
    private String userId;
    private String activityType;
    private String recommendations;
    private List<String> improvements;
    private List<String> safety;
    private List<String> suggestions;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime modifiesAt;

}
