package com.goatfarm.mapper;

import com.goatfarm.entity.BreedingRecord;
import com.goatfarm.entity.Farm;
import com.goatfarm.entity.Goat;
import com.goatfarm.entity.User;
import com.goatfarm.entity.VaccinationRecord;
import com.goatfarm.model.BreedingRecordData;
import com.goatfarm.model.FarmData;
import com.goatfarm.model.GoatData;
import com.goatfarm.model.LoginRequest;
import com.goatfarm.model.ResetPasswordRequest;
import com.goatfarm.model.UserData;
import com.goatfarm.model.VaccinationRecordData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MappersTest {

    @Test
    void clientSuppliedIdsAreNeverCopiedOntoNewEntities() {
        Farm farm = new Farm();
        farm.setFarmId(1L);
        Goat goat = new Goat();
        goat.setFarm(farm);

        GoatData goatData = new GoatData();
        goatData.setGoatId(777L);
        goatData.setTagNumber("G1");
        BreedingRecordData breeding = new BreedingRecordData();
        breeding.setBreedingId(888L);
        VaccinationRecordData vaccination = new VaccinationRecordData();
        vaccination.setVaccinationId(999L);
        FarmData farmData = new FarmData();
        farmData.setFarmId(555L);

        assertThat(GoatMapper.toEntity(goatData, farm).getGoatId()).isNull();
        assertThat(BreedingRecordMapper.toEntity(breeding, goat, goat).getBreedingId()).isNull();
        assertThat(VaccinationRecordMapper.toEntity(vaccination, goat).getVaccinationId()).isNull();
        assertThat(FarmMapper.toEntity(farmData).getFarmId()).isNull();
    }

    @Test
    void vaccinationRecordTakesTheFarmIdFromTheGoat() {
        Farm farm = new Farm();
        farm.setFarmId(42L);
        Goat goat = new Goat();
        goat.setFarm(farm);

        assertThat(VaccinationRecordMapper.toEntity(new VaccinationRecordData(), goat).getFarmId()).isEqualTo(42L);
    }

    @Test
    void profileUpdateNowChangesTheNameAndKeepsItWhenNoneIsSent() {
        User user = new User();
        user.setFullName("Old Name");
        UserData update = new UserData();
        update.setFullName("New Name");
        update.setPhone("123");

        UserMapper.applyUpdate(user, update, "new@b.com");

        assertThat(user.getFullName()).isEqualTo("New Name");
        assertThat(user.getEmail()).isEqualTo("new@b.com");
        assertThat(user.getPhone()).isEqualTo("123");

        update.setFullName(" ");
        UserMapper.applyUpdate(user, update, "new@b.com");
        assertThat(user.getFullName()).isEqualTo("New Name");
    }

    @Test
    void goatDtoWithRecordsCarriesBothRecordListsAndPlainDtoDoesNot() {
        Goat goat = new Goat();
        goat.setGoatId(1L);
        goat.setTagNumber("G1");
        VaccinationRecord vaccination = new VaccinationRecord();
        vaccination.setVaccineName("FMD");
        vaccination.setFarmId(9L);
        vaccination.setGoat(goat);
        goat.getVaccinationRecords().add(vaccination);
        BreedingRecord breeding = new BreedingRecord();
        breeding.setBreederTagNumber("B1");
        breeding.setGoat(goat);
        goat.getBreedingRecords().add(breeding);

        GoatData detailed = GoatMapper.toDtoWithRecords(goat);
        GoatData plain = GoatMapper.toDto(goat);

        assertThat(detailed.getVaccinationRecords()).hasSize(1);
        assertThat(detailed.getVaccinationRecords().get(0).getVaccineName()).isEqualTo("FMD");
        assertThat(detailed.getVaccinationRecords().get(0).getFarmId()).isEqualTo(9L);
        assertThat(detailed.getBreedingRecords()).hasSize(1);
        assertThat(detailed.getBreedingRecords().get(0).getBreederTagNumber()).isEqualTo("B1");
        assertThat(plain.getVaccinationRecords()).isNull();
        assertThat(plain.getBreedingRecords()).isNull();
    }

    @Test
    void userDtoNeverCarriesThePassword() {
        User user = new User();
        user.setUserId(1L);
        user.setPasswordHash("$2a$10$abcdefghijklmnopqrstuv");

        assertThat(UserMapper.toDto(user,false).getPasswordHash()).isNull();
    }

    @Test
    void credentialCarryingObjectsMaskThemselvesInToString() {
        UserData data = new UserData();
        data.setEmail("a@b.com");
        data.setPasswordHash("super-secret");

        assertThat(data.toString()).doesNotContain("super-secret");
        assertThat(new LoginRequest("a@b.com", "super-secret").toString()).doesNotContain("super-secret");
        assertThat(new ResetPasswordRequest("tok-123", "super-secret").toString())
                .doesNotContain("super-secret").doesNotContain("tok-123");
    }
}
