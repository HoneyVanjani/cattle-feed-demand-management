package com.amul.cattlefeed.service;

import com.amul.cattlefeed.entity.Farmer;
import java.util.List;

public interface FarmerService {

    Farmer saveFarmer(Farmer farmer);

    List<Farmer> getAllFarmers();

    Farmer getFarmerById(Long id);


}
