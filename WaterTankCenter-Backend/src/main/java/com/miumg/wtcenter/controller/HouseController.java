package com.miumg.wtcenter.controller;

import com.miumg.wtcenter.common.Pipes;
import com.miumg.wtcenter.common.PipesText;
import com.miumg.wtcenter.dto.HouseDto;
import com.miumg.wtcenter.dto.WaterTankRequestDto;
import com.miumg.wtcenter.service.WaterTankHouseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HouseController {

    private final WaterTankHouseService waterTankHouseService;

    @PostMapping("/houses")
    public ResponseEntity<Void> postHouse(@RequestBody WaterTankRequestDto request) {
        waterTankHouseService.addHouse(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pipes")
    public ResponseEntity<List<PipesText.PipesTextDto>> getAllPipes() {
        return ResponseEntity.ok(PipesText.toMap());
    }
}
