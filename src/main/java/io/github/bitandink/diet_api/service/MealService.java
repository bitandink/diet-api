package io.github.bitandink.diet_api.service;

import io.github.bitandink.diet_api.dto.MealRequest;
import io.github.bitandink.diet_api.dto.MealResponse;
import io.github.bitandink.diet_api.entity.Meal;
import io.github.bitandink.diet_api.exception.MealNotFoundException;
import io.github.bitandink.diet_api.repository.MealRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service

/*
 * 기본 트랜잭션 정책:
 * 조회 메서드는 readOnly = true
 *
 * findAll(), findById()는 데이터를 변경하지 않으므로
 * 읽기 전용 트랜잭션으로 실행한다.
 *
 * 쓰기 메서드는 아래에서 @Transactional로 다시 덮어쓴다.
 */
@Transactional(readOnly = true)
public class MealService {

    private final MealRepository mealRepository;

    public MealService(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }


    // 전체 식단 조회
    public List<MealResponse> findAll() {
        return mealRepository.findAll()
                .stream()
                .map(MealResponse::from)
                .toList();
    }


    // 특정 식단 조회
    public MealResponse findById(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new MealNotFoundException(id));

        return MealResponse.from(meal);
    }


    /*
     * 새로운 식단 등록
     *
     * 데이터를 생성하는 쓰기 작업이므로
     * readOnly = true 기본 설정을 일반 트랜잭션으로 덮어쓴다.
     */
    @Transactional
    public MealResponse saveMeal(MealRequest mealRequest) {

        Meal meal = new Meal(
                mealRequest.getMealName(),
                mealRequest.getCalories(),
                mealRequest.getProtein(),
                mealRequest.getCarbohydrate(),
                mealRequest.getFat()
        );

        Meal savedMeal = mealRepository.save(meal);

        return MealResponse.from(savedMeal);
    }


    /*
     * 기존 식단 수정
     *
     * 이 메서드는 Dirty Checking을 사용하므로
     * 반드시 쓰기 가능한 트랜잭션이 필요하다.
     *
     * 조회한 Meal은 영속 상태가 되고,
     * meal.update(...)로 값이 바뀌면
     * 트랜잭션 종료 시 Hibernate가 변경을 감지해 UPDATE 한다.
     */
    @Transactional
    public MealResponse updateMeal(Long id, MealRequest request) {

        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new MealNotFoundException(id));

        meal.update(
                request.getMealName(),
                request.getCalories(),
                request.getProtein(),
                request.getCarbohydrate(),
                request.getFat()
        );

        return MealResponse.from(meal);
    }


    /*
     * 식단 삭제
     *
     * delete 역시 데이터 변경 작업이므로
     * 일반 트랜잭션으로 실행한다.
     */
    @Transactional
    public void deleteMeal(Long id) {

        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new MealNotFoundException(id));

        mealRepository.delete(meal);
    }
}