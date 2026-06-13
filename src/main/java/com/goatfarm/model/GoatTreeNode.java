package com.goatfarm.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class GoatTreeNode {
    private String tagNumber;
    private String breed;
    private String gender;
    private LocalDate birthDate;
    private GoatTreeNode father;
    private GoatTreeNode mother;

    private boolean cycleDetected;
    private String missingReason;
}
