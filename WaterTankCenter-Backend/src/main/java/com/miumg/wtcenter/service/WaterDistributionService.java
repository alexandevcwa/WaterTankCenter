package com.miumg.wtcenter.service;

import com.miumg.wtcenter.dto.HouseDto;
import com.miumg.wtcenter.simulink.HouseLink;
import com.miumg.wtcenter.simulink.TankLink;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WaterDistributionService {

    private final ConcurrentHashMap<String, HouseLink> houses = new ConcurrentHashMap<>();
    private final TankLink tank;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final AtomicInteger count = new AtomicInteger(0);

    public WaterDistributionService(TankLink tank) {
        this.tank = tank;
    }

    public double getTankVolume() {
        return tank.getCurrentVolume();
    }

    public Map<String, HouseLink> getHouses() {
        return houses;
    }

    public void createHouse(HouseDto house) {
        HouseLink houseLink = new HouseLink(
                houses.size() + "_" + count.incrementAndGet(),
                house.n3PerSecond(),
                house.periodInSeconds(),
                tank
        );
        houses.put(houseLink.getId(), houseLink);
        executor.submit(houseLink);
    }
}
