package com.goatfarm.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GoatHistoryData {

  private GoatData goatDetails;
  private List<BreedingRecordData> breedingHistoryData;
  private List<VaccinationRecordData> vaccinationHistoryData;
  private GoatTreeNode goatFamilyHierarchy;
}
