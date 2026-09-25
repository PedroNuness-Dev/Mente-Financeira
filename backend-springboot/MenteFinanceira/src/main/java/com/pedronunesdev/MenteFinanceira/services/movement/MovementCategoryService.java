package com.pedronunesdev.MenteFinanceira.services.movement;

import com.pedronunesdev.MenteFinanceira.enums.movement.MovementCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class MovementCategoryService {

    public List<MovementCategory> findAllMovementCategories(){

        log.info("Starting search for all movement categories registered in the system");

        return Arrays.stream(MovementCategory.values())
                .toList();
    }
}