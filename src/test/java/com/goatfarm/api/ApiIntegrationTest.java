package com.goatfarm.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goatfarm.entity.User;
import com.goatfarm.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests of the HTTP contract through the real security chain and an in-memory H2 database.
 * Every test creates its own users/farms, so tests are independent of each other.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    // ------------------------------------------------------------------ helpers

    private static Map<String, Object> json(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private MockHttpServletRequestBuilder withBody(MockHttpServletRequestBuilder builder, String token, Object body) throws Exception {
        builder.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }

    private MvcResult send(MockHttpServletRequestBuilder builder, String token, Object body) throws Exception {
        return mockMvc.perform(withBody(builder, token, body)).andReturn();
    }

    private JsonNode read(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    /** Registers a user + farm and returns a token that carries the farm id. */
    private String signUpAndLogin(String email, String phone) throws Exception {
        MvcResult user = send(post("/api/user/addUser"), null,
                json("fullName", "Tester", "email", email, "passwordHash", "secret1", "phone", phone));
        assertThat(user.getResponse().getStatus()).isEqualTo(200);
        long userId = read(user).get("userId").asLong();

        MvcResult farm = send(post("/api/farm/addFarm"), null,
                json("farmName", "Farm of " + email, "location", "Nashik", "size", 5.0, "goatTypes", "BOER", "userId", userId));
        assertThat(farm.getResponse().getStatus()).isEqualTo(200);
        return login(email, "secret1");
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = send(post("/api/auth/login"), null, json("email", email, "password", password));
        assertThat(result.getResponse().getStatus()).as("login status").isEqualTo(200);
        return read(result).get("token").asText();
    }

    private void addGoat(String token, String tag, String gender, String father, String mother) throws Exception {
        Map<String, Object> body = json("tagNumber", tag, "gender", gender, "breed", "Boer");
        if (father != null) body.put("fatherTagNumber", father);
        if (mother != null) body.put("motherTagNumber", mother);
        assertThat(send(post("/api/goat"), token, body).getResponse().getStatus()).as("add goat " + tag).isEqualTo(200);
    }

    // ------------------------------------------------------------------ authentication & transport

    @Test
    void protectedEndpointsAnswer401WithoutAValidToken() throws Exception {
        mockMvc.perform(get("/api/goat/allGoats")).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/goat/allGoats").header("Authorization", "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid JWT token"));
    }

    @Test
    void aStaleTokenDoesNotBreakPublicEndpoints() throws Exception {
        mockMvc.perform(get("/api/auth/health").header("Authorization", "Bearer stale.token.value"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Service is up and running"));
    }

    @Test
    void malformedJsonAndWrongMethodsGetProper4xxStatuses() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        mockMvc.perform(request(HttpMethod.PUT, "/api/auth/health"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void loginIsCaseInsensitiveAndFailuresAreUniform() throws Exception {
        String email = uniqueEmail();
        signUpAndLogin(email, "9000000001");

        login(email.toUpperCase(), "secret1");

        mockMvc.perform(withBody(post("/api/auth/login"), null, json("email", email, "password", "wrong")))
                .andExpect(status().isUnauthorized()).andExpect(content().string("Invalid credentials"));
        mockMvc.perform(withBody(post("/api/auth/login"), null, json("email", "ghost@example.com", "password", "x")))
                .andExpect(status().isUnauthorized()).andExpect(content().string("Invalid credentials"));
    }

    // ------------------------------------------------------------------ users, passwords

    @Test
    void registrationNormalisesTheEmailStoresBcryptAndNeverReturnsThePassword() throws Exception {
        String email = uniqueEmail();

        MvcResult result = send(post("/api/user/addUser"), null,
                json("fullName", "Asha", "email", email.toUpperCase(), "passwordHash", "secret1", "phone", "9000000002"));

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        JsonNode body = read(result);
        assertThat(body.get("email").asText()).isEqualTo(email.toLowerCase());
        assertThat(body.has("passwordHash")).isFalse();
        assertThat(userRepository.findByEmailIgnoreCase(email).orElseThrow().getPasswordHash()).startsWith("$2");

        MvcResult duplicate = send(post("/api/user/addUser"), null,
                json("fullName", "Asha", "email", email, "passwordHash", "secret1", "phone", "9000000002"));
        assertThat(duplicate.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    void shortPasswordsAreRejectedAtRegistration() throws Exception {
        mockMvc.perform(withBody(post("/api/user/addUser"), null,
                        json("fullName", "A", "email", uniqueEmail(), "passwordHash", "abc", "phone", "1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Password must be at least 6 characters"));
    }

    @Test
    void legacyPlaintextAccountsStillLogInAndAreUpgraded() throws Exception {
        String email = uniqueEmail();
        User legacy = new User();
        legacy.setEmail(email);
        legacy.setFullName("Legacy");
        legacy.setPasswordHash("oldpass1");
        userRepository.save(legacy);

        login(email, "oldpass1");

        assertThat(userRepository.findByEmailIgnoreCase(email).orElseThrow().getPasswordHash()).startsWith("$2");
        login(email, "oldpass1"); // and keeps working after the upgrade
    }

    @Test
    void passwordResetFlowWorksOnceAndDoesNotRevealWhichPartWasWrong() throws Exception {
        String email = uniqueEmail();
        signUpAndLogin(email, "9000000003");

        mockMvc.perform(withBody(post("/api/auth/forgot-password/verify"), null, json("email", email, "phone", "0000")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username and phone number do not match"));
        mockMvc.perform(withBody(post("/api/auth/forgot-password/verify"), null, json("email", "ghost@example.com", "phone", "0000")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username and phone number do not match"));

        MvcResult verified = send(post("/api/auth/forgot-password/verify"), null, json("email", email, "phone", "9000000003"));
        assertThat(verified.getResponse().getStatus()).isEqualTo(200);
        String resetToken = read(verified).get("resetToken").asText();
        assertThat(read(verified).get("success").asBoolean()).isTrue();

        mockMvc.perform(withBody(post("/api/auth/forgot-password/reset"), null, json("resetToken", resetToken, "newPassword", "brandnew1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password updated successfully"));
        login(email, "brandnew1");

        mockMvc.perform(withBody(post("/api/auth/forgot-password/reset"), null, json("resetToken", resetToken, "newPassword", "another1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid or used reset token"));
    }

    // ------------------------------------------------------------------ farm

    @Test
    void farmIdFromTheClientIsIgnoredAndAUserCanOnlyRegisterOneFarm() throws Exception {
        String email = uniqueEmail();
        MvcResult user = send(post("/api/user/addUser"), null,
                json("fullName", "A", "email", email, "passwordHash", "secret1", "phone", "1"));
        long userId = read(user).get("userId").asLong();

        MvcResult first = send(post("/api/farm/addFarm"), null,
                json("farmId", 424242, "farmName", "F", "location", "L", "size", 1.0, "goatTypes", "BOER", "userId", userId));
        assertThat(first.getResponse().getStatus()).isEqualTo(200);
        assertThat(read(first).get("farmId").asLong()).isNotEqualTo(424242L);
        assertThat(read(first).get("userId").asLong()).isEqualTo(userId);
        assertThat(read(first).get("farmer").has("passwordHash")).isFalse();

        MvcResult second = send(post("/api/farm/addFarm"), null,
                json("farmName", "Second", "location", "L", "size", 1.0, "goatTypes", "BOER", "userId", userId));
        assertThat(second.getResponse().getStatus()).isEqualTo(400);
    }

    @Test
    void farmEndpointsReadAndUpdateTheCallersFarm() throws Exception {
        String token = signUpAndLogin(uniqueEmail(), "9000000004");

        mockMvc.perform(get("/api/farm").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.location").value("Nashik"))
                .andExpect(jsonPath("$.farmer.passwordHash").doesNotExist());

        mockMvc.perform(withBody(put("/api/farm"), token, json("goatTypes", "BEETAL ,BOER", "size", 12.5)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.goatTypes").value("BEETAL, BOER"));
    }

    // ------------------------------------------------------------------ goats

    @Test
    void goatCrudKeepsTheOriginalJsonShapeAndRejectsDuplicates() throws Exception {
        String token = signUpAndLogin(uniqueEmail(), "9000000005");

        MvcResult created = send(post("/api/goat"), token, json("goatId", 777, "tagNumber", "G1", "gender", "FEMALE", "breed", "Boer"));
        assertThat(created.getResponse().getStatus()).isEqualTo(200);
        assertThat(read(created).get("goatId").asLong()).isNotEqualTo(777L);
        assertThat(read(created).get("vaccinationRecords").isArray()).isTrue();
        assertThat(read(created).get("breedingRecords").isArray()).isTrue();

        mockMvc.perform(withBody(post("/api/goat"), token, json("tagNumber", "G1", "gender", "FEMALE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("already exists")));
        mockMvc.perform(withBody(post("/api/goat"), token, json("tagNumber", " ", "gender", "FEMALE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("tagNumber is required"));

        mockMvc.perform(get("/api/goat/allGoats").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/goat/tag/G1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tagNumber").value("G1"));
        mockMvc.perform(get("/api/goat/tag/NOPE").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound()).andExpect(content().string("Not Found: Goat not found with tag: NOPE"));

        mockMvc.perform(withBody(put("/api/goat/update/G1"), token, json("tagNumber", "G1", "gender", "FEMALE", "weight", 42.5)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.weight").value(42.5));
        mockMvc.perform(delete("/api/goat/delete/G1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Goat deleted successfully"));
        mockMvc.perform(get("/api/goat/allGoats").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", empty()));
    }

    @Test
    void farmsAreIsolatedFromEachOther() throws Exception {
        String tokenA = signUpAndLogin(uniqueEmail(), "9000000006");
        String tokenB = signUpAndLogin(uniqueEmail(), "9000000007");
        addGoat(tokenA, "ISO1", "FEMALE", null, null);
        addGoat(tokenA, "ISO-SIRE", "MALE", null, null);

        mockMvc.perform(get("/api/goat/tag/ISO1").header("Authorization", "Bearer " + tokenB)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/goat/allGoats").header("Authorization", "Bearer " + tokenB)).andExpect(jsonPath("$", empty()));

        MvcResult breeding = send(post("/api/breeding"), tokenA,
                json("goatTagNumber", "ISO1", "breederTagNumber", "ISO-SIRE", "breedingDate", "2026-01-01", "pregnancyStatus", "PREGNANT"));
        long breedingId = read(breeding).get("breedingId").asLong();

        mockMvc.perform(withBody(put("/api/breeding/" + breedingId), tokenB, json("pregnancyStatus", "DELIVERED")))
                .andExpect(status().isNotFound());
    }

    @Test
    void goatDetailsIncludeHistoryAndPedigree() throws Exception {
        String token = signUpAndLogin(uniqueEmail(), "9000000008");
        addGoat(token, "GF", "MALE", null, null);
        addGoat(token, "F", "MALE", "GF", null);
        addGoat(token, "KID", "FEMALE", "F", null);

        mockMvc.perform(get("/api/goat/details/KID?depth=3").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goatDetails.tagNumber").value("KID"))
                .andExpect(jsonPath("$.goatFamilyHierarchy.father.father.tagNumber").value("GF"))
                .andExpect(jsonPath("$.breedingHistoryData", empty()))
                .andExpect(jsonPath("$.vaccinationHistoryData", empty()));
    }

    // ------------------------------------------------------------------ breeding

    @Test
    void breedingRulesInbreedingPregnancyAndDelivery() throws Exception {
        String token = signUpAndLogin(uniqueEmail(), "9000000009");
        addGoat(token, "B0", "MALE", null, null);
        addGoat(token, "G1", "FEMALE", "B0", null);
        addGoat(token, "B1", "MALE", "B0", null);   // sibling of G1
        addGoat(token, "B2", "MALE", null, null);   // unrelated

        mockMvc.perform(withBody(post("/api/breeding"), token,
                        json("goatTagNumber", "G1", "breederTagNumber", "B1", "breedingDate", "2026-01-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Sibling inbreeding detected!"));
        mockMvc.perform(withBody(post("/api/breeding"), token,
                        json("goatTagNumber", "G1", "breederTagNumber", "B0", "breedingDate", "2026-01-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Parent-child relation detected!"));

        mockMvc.perform(withBody(post("/api/breeding"), token,
                        json("goatTagNumber", "G1", "breederTagNumber", "B2", "breedingDate", "2026-01-01", "pregnancyStatus", "PREGNANT")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expectedKiddingDate").value("2026-05-31"));

        mockMvc.perform(withBody(post("/api/breeding"), token,
                        json("goatTagNumber", "G1", "breederTagNumber", "B2", "breedingDate", "2026-02-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("active pregnancy")));

        mockMvc.perform(withBody(post("/api/breeding/check-inbreeding"), token, json("goatTagNumber", "G1", "breederTagNumber", "B1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isInbreeding").value(true))
                .andExpect(jsonPath("$.message").value("Sibling inbreeding detected!"))
                .andExpect(jsonPath("$.goatTree.tagNumber").value("G1"))
                .andExpect(jsonPath("$.safeBreeders[0]").value("B2"));
        mockMvc.perform(withBody(post("/api/breeding/check-inbreeding"), token, json("goatTagNumber", "G1", "breederTagNumber", "B2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isInbreeding").value(false))
                .andExpect(jsonPath("$.message").value("Safe for breeding!"))
                .andExpect(jsonPath("$.safeBreeders", empty()));
        mockMvc.perform(withBody(post("/api/breeding/check-inbreeding"), token, json("goatTagNumber", "", "breederTagNumber", "B2")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/breeding/latest-pregnant/G1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pregnancyStatus").value("PREGNANT"));

        mockMvc.perform(withBody(put("/api/breeding/delivery/G1"), token,
                        json("pregnancyStatus", "DELIVERED", "deliveryDate", "2026-05-30", "offspringCount", 3, "kidsAlive", 2, "kidsDead", 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.kidsAlive").value(2));
        mockMvc.perform(get("/api/breeding/latest-pregnant/G1").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Not Found: No active pregnancy found for the goat"));
    }

    @Test
    void deletingASireKeepsTheBreedingHistoryOfTheDam() throws Exception {
        String token = signUpAndLogin(uniqueEmail(), "9000000010");
        addGoat(token, "DAM", "FEMALE", null, null);
        addGoat(token, "SIRE", "MALE", null, null);
        mockMvc.perform(withBody(post("/api/breeding"), token,
                        json("goatTagNumber", "DAM", "breederTagNumber", "SIRE", "breedingDate", "2026-01-01", "pregnancyStatus", "PREGNANT")))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/goat/delete/SIRE").header("Authorization", "Bearer " + token)).andExpect(status().isOk());

        mockMvc.perform(get("/api/breeding/history/DAM").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].breederTagNumber").value("SIRE"));
    }

    @Test
    void upcomingDeliveriesValidateTheirWindow() throws Exception {
        String token = signUpAndLogin(uniqueEmail(), "9000000011");

        mockMvc.perform(get("/api/breeding/upcoming-deliveries").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$", empty()));
        mockMvc.perform(get("/api/breeding/upcoming-deliveries?pastDays=-1").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("pastDays must be >= 0"));
    }

    // ------------------------------------------------------------------ vaccinations

    @Test
    void vaccinationScheduleAndTheTwentyOneDayRule() throws Exception {
        String token = signUpAndLogin(uniqueEmail(), "9000000012");
        addGoat(token, "V1", "FEMALE", null, null);

        mockMvc.perform(withBody(post("/api/vaccinations"), token,
                        json("goatTagNumber", "V1", "vaccineName", "FMD", "vaccinationDate", "2026-01-01")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nextVaccineName").value("FMD Booster"))
                .andExpect(jsonPath("$.nextVaccinationDate").value("2026-01-22"))
                .andExpect(jsonPath("$.goatTagNumber").value("V1"));

        mockMvc.perform(withBody(post("/api/vaccinations"), token,
                        json("goatTagNumber", "V1", "vaccineName", "PPR", "vaccinationDate", "2026-01-10")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("within 21 days")));

        mockMvc.perform(withBody(post("/api/vaccinations"), token, json("goatTagNumber", "V1", "vaccinationDate", "2026-03-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Vaccine name is required"));

        mockMvc.perform(get("/api/vaccinations/history/V1").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/vaccinations/schedule/tomorrow").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$", empty()));
        mockMvc.perform(get("/api/vaccinations/upcoming").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/goat/allGoats").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[?(@.tagNumber=='V1')].vaccinationRecords[0].vaccineName").value("FMD"));
    }
}
