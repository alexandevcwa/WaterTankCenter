package com.miumg.wtcenter.controller;

import com.miumg.wtcenter.dto.HouseDto;
import com.miumg.wtcenter.service.WaterTankHouseService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HouseController {

    private final WaterTankHouseService waterTankHouseService;

    @PostMapping("/houses")
    public ResponseEntity<Void> postHouse(@RequestBody HouseDto houseDto){
        waterTankHouseService.addHouse(houseDto);
        return ResponseEntity.ok().build();
    }

    public List<HouseDto> getAll(){
        return null;
    }
}
