package io.github.bitandink.diet_api.repository;

import io.github.bitandink.diet_api.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRepository extends JpaRepository<Meal, Long> {
}