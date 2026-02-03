package com.td.springBatch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Employee {
    @Id
    private Integer employeeId;
    private String empName;
    private String empDept;
}
