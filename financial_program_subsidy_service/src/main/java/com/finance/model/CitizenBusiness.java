package com.finance.model;
 
import java.util.List;
 
import com.finance.enums.Status;
import com.finance.enums.Type;
 
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
 
@Entity
@Table(name = "citizen_business")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CitizenBusiness {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long entityId;
 
    @NotBlank(message = "Name is required")
    @Size(max = 15, message = "Name must not exceed 15 characters")
    private String name;
 
    @NotNull(message = "Type is required")
    @Enumerated(EnumType.STRING)
    private Type type;
 
    private String address;
 
    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Only digits allowed")
    private String contactInfo;
 
    @Enumerated(EnumType.STRING)
    private Status status;
 
    @OneToMany(mappedBy = "citizenBusiness")
    private List<EntityDocument> documents;
}