package com.iroom.sensor.repository.service;

import com.iroom.sensor.dto.HeavyEquipment.EquipmentRegisterRequest;
import com.iroom.sensor.dto.HeavyEquipment.EquipmentUpdateLocationRequest;
import com.iroom.sensor.entity.HeavyEquipment;
import com.iroom.sensor.repository.HeavyEquipmentRepository;
import com.iroom.sensor.service.HeavyEquipmentService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class EquipmentServiceTest {

    @Mock
    private HeavyEquipmentRepository equipmentRepository;

    @InjectMocks
    private HeavyEquipmentService equipmentService;

    @Test
    @DisplayName("장비 등록 요청 시 저장 후 응답 반환 테스트")
    void registerTest(){
        EquipmentRegisterRequest request = new EquipmentRegisterRequest("크레인A-1","크레인",15.0);
        HeavyEquipment equipment = HeavyEquipment.builder()
                .name("크레인A-1")
                .type("크레인")
                .radius(15.0)
                .build();
        setIdViaReflection(equipment, 1L);

        given(equipmentRepository.save(any(HeavyEquipment.class))).willReturn(equipment);

        var response = equipmentService.register(request);

        assertThat(response.name()).isEqualTo("크레인A-1");
        assertThat(response.type()).isEqualTo("크레인");
        assertThat(response.radius()).isEqualTo(15.0);
    }

    @Test
    @DisplayName("장비 ID로 위치 업데이트 요청 시 위치 변경 테스트")
    void updateLocationTest(){
        Long equipmentId = 1L;
        String newLocation = "35.8343, 128.4723";
        EquipmentUpdateLocationRequest request = new EquipmentUpdateLocationRequest(equipmentId, newLocation);
        HeavyEquipment equipment = HeavyEquipment.builder().build();
        setIdViaReflection(equipment, equipmentId);

        given(equipmentRepository.findById(equipmentId)).willReturn(java.util.Optional.of(equipment));

        var response = equipmentService.updateLocation(request);

        assertThat(response.id()).isEqualTo(equipmentId);
        assertThat(response.location()).isEqualTo(newLocation);
    }

    @Test
    @DisplayName("없는 ID로 위치 업데이트 시 예외 발생 테스트")
    void updateLocation_notFoundId(){
        Long invalidId = 999L;
        EquipmentUpdateLocationRequest request = new EquipmentUpdateLocationRequest(invalidId, "354.8343, 128.4723");
        given(equipmentRepository.findById(invalidId)).willReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> equipmentService.updateLocation(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("장비 없음");
    }

    //테스트를 위한 id 값 주입
    private void setIdViaReflection(HeavyEquipment entity, Long id){
        try{
            java.lang.reflect.Field field = HeavyEquipment.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
