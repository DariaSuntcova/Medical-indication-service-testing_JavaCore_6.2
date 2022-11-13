package ru.netology.patient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.medical.MedicalServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

public class MedicalServiceImplTest {
    PatientInfoFileRepository repository = Mockito.mock(PatientInfoFileRepository.class);
    SendAlertService alertService = Mockito.mock(SendAlertService.class);
    MedicalServiceImpl medicalService = new MedicalServiceImpl(repository, alertService);
    String patientId = "1";

    @BeforeEach
    public void setup() {

        Mockito.when(repository.getById(patientId))
                .thenReturn(new PatientInfo(patientId,
                        "Иван", "Петров", LocalDate.of(1980, 11, 26),
                        new HealthInfo(new BigDecimal("36.65"), new BloodPressure(120, 80))));

    }

    @ParameterizedTest
    @MethodSource("argumentsStreamByCheckBloodPressureTest")
    public void checkBloodPressureTestByMockitoVerify(BloodPressure bloodPressure, int count) {

        medicalService.checkBloodPressure(patientId, bloodPressure);

        Mockito.verify(alertService, Mockito.times(count)).send(Mockito.any());
    }

    @ParameterizedTest
    @MethodSource("argumentsStreamByCheckBloodPressureTest")
    public void checkBloodPressureTestByArgumentCaptor(BloodPressure bloodPressure, int count) {
        if (count == 0) {
            return;
        }
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        medicalService.checkBloodPressure(patientId, bloodPressure);

        Mockito.verify(alertService).send(argumentCaptor.capture());

        String exp = String.format("Warning, patient with id: %s, need help", patientId);

        Assertions.assertEquals(exp, argumentCaptor.getValue());
    }

    public static Stream<Arguments> argumentsStreamByCheckBloodPressureTest() {
        return Stream.of(
                Arguments.of(new BloodPressure(120, 80), 0),
                Arguments.of(new BloodPressure(100, 100), 1),
                Arguments.of(new BloodPressure(120, 100), 1),
                Arguments.of(new BloodPressure(100, 80), 1),
                Arguments.of(new BloodPressure(150, 80), 1),
                Arguments.of(new BloodPressure(120, 100), 1)
        );
    }


    @ParameterizedTest
    @MethodSource("argumentsStreamByCheckTemperatureTest")
    public void checkTemperatureTestByMockitoVerify(BigDecimal temperature, int count) {

        medicalService.checkTemperature(patientId, temperature);

        Mockito.verify(alertService, Mockito.times(count)).send(Mockito.any());

    }

    @ParameterizedTest
    @MethodSource("argumentsStreamByCheckTemperatureTest")
    public void checkTemperatureTestByArgumentCaptor(BigDecimal temperature, int count) {
        if (count == 0) {
            return;
        }
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        medicalService.checkTemperature(patientId, temperature);

        Mockito.verify(alertService).send(argumentCaptor.capture());

        String exp = String.format("Warning, patient with id: %s, need help", patientId);

        Assertions.assertEquals(exp, argumentCaptor.getValue());


    }

    public static Stream<Arguments> argumentsStreamByCheckTemperatureTest() {
        return Stream.of(
                Arguments.of(new BigDecimal("36.65"), 0),
                Arguments.of(new BigDecimal("35.15"), 0),
                Arguments.of(new BigDecimal("35.14"), 1),
                Arguments.of(new BigDecimal("38.15"), 0),
                Arguments.of(new BigDecimal("38.16"), 1) //???
        );
    }


}
