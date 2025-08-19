package com.zuzu.review.ingestion;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Data
@Table(
    name = "ingestion_files",
    uniqueConstraints = @UniqueConstraint(columnNames = "fileName")
)
public class IngestionFile {

    @Id
    private String id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false)
    private Instant processedAt;
    
    private String error;
    

    // TODO clean
    //public IngestionFile() {}

    public IngestionFile(String fileName) {
        this.fileName = fileName;
        this.processedAt = Instant.now();
    }
    
    //TODO need to clean up as lombock is there
    public String getId() { return id; }
    public String getFileName() { return fileName; }
    public Instant getProcessedAt() { return processedAt; }
    public String getStatus() { return status; }
}
