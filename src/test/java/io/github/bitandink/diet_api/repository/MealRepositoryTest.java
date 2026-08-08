package io.github.bitandink.diet_api.repository;

import io.github.bitandink.diet_api.entity.Meal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManager;


/*
 * @DataJpaTest
 *
 * JPA와 Repository 계층을 테스트하기 위한 Spring Boot 테스트 슬라이스.
 *
 * Controller나 Service 같은 애플리케이션 전체를 띄우는 것이 아니라
 * JPA 테스트에 필요한 구성만 로딩한다.
 *
 *
 * 지금까지 작성했던 MealServiceTest와 가장 큰 차이:
 *
 * MealServiceTest
 * ------------------------------
 * MealService
 *      ↓
 * MealRepository(Mock)
 *
 * 실제 DB 사용 X
 *
 *
 * MealRepositoryTest
 * ------------------------------
 * MealRepository
 *      ↓
 * JPA / Hibernate
 *      ↓
 * H2 Database
 *
 * 실제 Repository와 실제 DB를 사용한다.
 *
 *
 * 따라서 여기서는 Mockito의
 *
 * @Mock
 * when(...)
 * verify(...)
 *
 * 를 사용하지 않는다.
 *
 * Repository가 진짜로 DB에 데이터를 저장하고
 * 다시 조회할 수 있는지를 확인하는 테스트다.
 *
 *
 * @DataJpaTest는 기본적으로 각 테스트를 트랜잭션 안에서 실행하고,
 * 테스트 종료 후 rollback한다.
 *
 * 그래서 테스트 데이터가 다음 테스트에 남지 않는다.
 */
@DataJpaTest
class MealRepositoryTest {


    /*
     * 실제 MealRepository Bean을 주입받는다.
     *
     * MealServiceTest에서는 @Mock이었지만
     * 여기서는 진짜 Spring Data JPA Repository다.
     *
     * 즉:
     *
     * mealRepository.save(...)
     *
     * 를 호출하면 실제로 Hibernate를 통해
     * 테스트용 H2 DB에 저장된다.
     */
    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private EntityManager entityManager;

    /*
     * ============================================================
     * Meal 저장 + 조회
     * ============================================================
     */


    @Test
    @DisplayName("Meal을 저장한 후 DB에서 다시 조회할 수 있다")
    void saveAndFindById() {

        // given
        Meal meal = new Meal(
                "닭가슴살 샐러드",
                350,
                40,
                30,
                10
        );

        /*
         * when - 저장
         *
         * Repository를 통해 Meal을 저장한다.
         */
        Meal savedMeal = mealRepository.save(meal);

        Long savedId = savedMeal.getId();

        /*
         * flush()
         *
         * 현재 영속성 컨텍스트에 쌓여 있는 변경사항을
         * DB에 즉시 반영한다.
         *
         * 쉽게 말하면:
         *
         * JPA가 관리하고 있는 변경사항
         *          ↓
         *       flush()
         *          ↓
         * 실제 INSERT SQL을 DB에 반영
         */
        entityManager.flush();

        /*
         * clear()
         *
         * 영속성 컨텍스트(1차 캐시)를 비운다.
         *
         * 이걸 하지 않으면 findById()를 호출했을 때
         * DB를 다시 조회하지 않고 이미 JPA가 관리하고 있는
         * savedMeal을 반환할 수 있다.
         *
         * clear() 이후에는 해당 Entity를 JPA가 기억하지 못하므로
         * 다시 조회하려면 DB에서 SELECT해야 한다.
         */
        entityManager.clear();


        /*
         * Persistence Context를 비운 상태에서 조회.
         *
         * 따라서 이 조회는 단순히 1차 캐시의 객체를 가져오는 것이 아니라
         * DB에 저장된 데이터를 다시 읽어오는 흐름을 검증할 수 있다.
         */
        Meal foundMeal = mealRepository.findById(savedId)
                .orElseThrow();


        // then
        assertEquals("닭가슴살 샐러드", foundMeal.getMealName());
        assertEquals(350, foundMeal.getCalories());
        assertEquals(40, foundMeal.getProtein());
        assertEquals(30, foundMeal.getCarbohydrate());
        assertEquals(10, foundMeal.getFat());
    }

    @Test
    @DisplayName("조회한 Meal의 값을 변경하면 Dirty Checking으로 DB에 반영된다")
    void updateByDirtyChecking() {

        // given
        Meal meal = new Meal(
                "닭가슴살 샐러드",
                350,
                40,
                30,
                10
        );

        /*
         * 먼저 테스트할 Meal을 실제 DB에 저장한다.
         */
        Meal savedMeal = mealRepository.save(meal);

        Long savedId = savedMeal.getId();


        /*
         * INSERT를 DB에 반영하고
         * 영속성 컨텍스트를 초기화한다.
         *
         * 이후 조회는 DB에서 다시 이루어진다.
         */
        entityManager.flush();
        entityManager.clear();


        /*
         * DB에서 Meal을 다시 조회한다.
         *
         * 이 순간 foundMeal은 다시
         * 영속성 컨텍스트가 관리하는 Entity가 된다.
         */
        Meal foundMeal = mealRepository.findById(savedId)
                .orElseThrow();


        /*
         * when
         *
         * Entity의 값을 변경한다.
         *
         * 여기서 중요한 점:
         *
         * mealRepository.save(foundMeal)
         *
         * 을 호출하지 않는다.
         */
        foundMeal.update(
                "연어 샐러드",
                450,
                35,
                20,
                25
        );


        /*
         * flush()가 발생하면 Hibernate는
         * 처음 조회했을 때의 Entity 상태와
         * 현재 Entity 상태를 비교한다.
         *
         * 값이 변경된 것을 발견하면 UPDATE SQL을 실행한다.
         *
         * 이것이 Dirty Checking이다.
         */
        entityManager.flush();


        /*
         * 다시 영속성 컨텍스트를 비운다.
         *
         * 이후 조회 결과가 메모리에 있던 foundMeal이 아니라
         * 실제 DB의 수정 결과인지 확인하기 위해서다.
         */
        entityManager.clear();


        /*
         * DB에서 다시 조회한다.
         */
        Meal updatedMeal = mealRepository.findById(savedId)
                .orElseThrow();


        // then
        assertEquals("연어 샐러드", updatedMeal.getMealName());
        assertEquals(450, updatedMeal.getCalories());
        assertEquals(35, updatedMeal.getProtein());
        assertEquals(20, updatedMeal.getCarbohydrate());
        assertEquals(25, updatedMeal.getFat());
    }
}