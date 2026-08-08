package io.github.bitandink.diet_api.service;

import io.github.bitandink.diet_api.dto.MealRequest;
import io.github.bitandink.diet_api.dto.MealResponse;
import io.github.bitandink.diet_api.entity.Meal;
import io.github.bitandink.diet_api.exception.MealNotFoundException;
import io.github.bitandink.diet_api.repository.MealRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/*
 * @ExtendWith(MockitoExtension.class)
 *
 * 이 테스트에서는 Spring Context를 실행하지 않는다.
 *
 * 즉:
 *
 * @SpringBootTest
 * @WebMvcTest
 *
 * 같은 Spring 테스트가 아니라
 * 순수 JUnit + Mockito 기반의 단위 테스트(Unit Test)다.
 *
 *
 * 테스트 대상:
 *
 * MealService
 *
 *
 * 테스트 범위:
 *
 * MealService
 *      ↓
 * MealRepository(Mock)
 *
 *
 * 실제 DB는 사용하지 않는다.
 *
 * Repository가 어떤 값을 반환한다고 "가정"하고,
 * 그 상황에서 Service가 올바르게 동작하는지 확인한다.
 *
 *
 * 장점:
 *
 * - 테스트가 빠르다.
 * - DB 환경에 영향을 받지 않는다.
 * - Service 로직만 독립적으로 검증할 수 있다.
 */
@ExtendWith(MockitoExtension.class)
class MealServiceTest {


    /*
     * @Mock
     *
     * 실제 MealRepository 대신 사용할 가짜 객체(Mock)를 만든다.
     *
     * 실제 Repository라면:
     *
     * mealRepository.findById(...)
     *      ↓
     * DB 조회
     *
     * 가 발생하지만,
     *
     * 테스트에서는 DB를 사용하지 않고
     * when(...).thenReturn(...)으로
     * 반환값을 직접 지정한다.
     */
    @Mock
    private MealRepository mealRepository;


    /*
     * @InjectMocks
     *
     * 테스트 대상 객체인 MealService를 생성하고,
     * 위에서 만든 @Mock MealRepository를 주입한다.
     *
     * 개념적으로는 다음과 비슷하다.
     *
     * MealRepository mockRepository = mock(MealRepository.class);
     * MealService mealService = new MealService(mockRepository);
     *
     *
     * 따라서 테스트 중 MealService가 Repository를 호출하면
     * 실제 Repository가 아니라 Mock Repository가 호출된다.
     */
    @InjectMocks
    private MealService mealService;


    /*
     * 여러 테스트에서 반복해서 사용할 Meal Entity.
     *
     * 매 테스트마다 동일한 Meal 객체를 새로 만들지 않기 위해
     * 필드로 선언해 둔다.
     */
    private Meal meal;


    /*
     * 여러 테스트에서 반복해서 사용할 MealRequest.
     *
     * 등록, 수정 실패 테스트 등에서 사용한다.
     */
    private MealRequest mealRequest;


    /*
     * @BeforeEach
     *
     * 각각의 @Test가 실행되기 전에 매번 실행된다.
     *
     * 즉 테스트가 8개라면 setUp()도 8번 실행된다.
     *
     * 중요한 이유:
     *
     * 각 테스트가 서로 같은 객체 상태를 공유하지 않고
     * 항상 깨끗한 초기 상태에서 시작할 수 있다.
     *
     * 특히 Meal은 updateMeal 테스트에서 상태가 변경되기 때문에
     * 테스트마다 새로 생성하지 않으면
     * 다른 테스트에 영향을 줄 수 있다.
     */
    @BeforeEach
    void setUp() {

        /*
         * Service가 Repository에서 조회했다고 가정할 Meal Entity.
         */
        meal = new Meal(
                "닭가슴살 샐러드",
                350,
                40,
                30,
                10
        );


        /*
         * 클라이언트에서 들어온 요청을 Service가 받았다고 가정할 DTO.
         *
         * Controller 테스트와 달리
         * Service 테스트에서는 JSON 변환을 하지 않는다.
         *
         * MealRequest 객체 자체를 직접 Service에 넘긴다.
         */
        mealRequest = new MealRequest();
        mealRequest.setMealName("닭가슴살 샐러드");
        mealRequest.setCalories(350);
        mealRequest.setProtein(40);
        mealRequest.setCarbohydrate(30);
        mealRequest.setFat(10);
    }


    /*
     * ============================================================
     * 전체 조회
     * ============================================================
     */


    @Test
    @DisplayName("전체 식단을 조회한다")
    void findAll() {

        /*
         * given
         *
         * Repository가 Meal 두 개를 가지고 있다고 가정한다.
         *
         * 첫 번째 Meal은 setUp()에서 만든 meal이고,
         * 두 번째 Meal을 여기서 추가로 만든다.
         */
        Meal meal2 = new Meal(
                "고구마",
                200,
                3,
                45,
                1
        );


        /*
         * 실제 DB 대신 Mock Repository의 동작을 지정한다.
         *
         * mealRepository.findAll()이 호출되면
         *
         * List.of(meal, meal2)
         *
         * 를 반환하도록 만든다.
         */
        when(mealRepository.findAll())
                .thenReturn(List.of(meal, meal2));


        /*
         * when
         *
         * 실제 테스트 대상인 MealService.findAll()을 호출한다.
         *
         * Service 내부에서는 대략:
         *
         * mealRepository.findAll()
         *      ↓
         * List<Meal>
         *      ↓
         * MealResponse로 변환
         *      ↓
         * List<MealResponse>
         *
         * 흐름으로 동작한다.
         */
        List<MealResponse> responses = mealService.findAll();


        /*
         * then
         *
         * Service가 Meal 두 개를
         * MealResponse 두 개로 변환해서 반환했는지 확인한다.
         */
        assertEquals(2, responses.size());


        /*
         * 첫 번째 Entity의 데이터가
         * 첫 번째 Response DTO에 정상적으로 들어갔는지 확인.
         */
        assertEquals(
                "닭가슴살 샐러드",
                responses.get(0).getMealName()
        );

        assertEquals(
                350,
                responses.get(0).getCalories()
        );


        /*
         * 두 번째 Entity도 Response DTO로
         * 정상 변환됐는지 확인.
         */
        assertEquals(
                "고구마",
                responses.get(1).getMealName()
        );

        assertEquals(
                200,
                responses.get(1).getCalories()
        );


        /*
         * Service가 실제로 Repository의 findAll()을
         * 호출했는지 확인한다.
         *
         * 반환 결과만 맞는 것이 아니라
         * Service → Repository 연결도 검증한다.
         */
        verify(mealRepository).findAll();
    }


    /*
     * ============================================================
     * ID 단건 조회 - 성공
     * ============================================================
     */


    @Test
    @DisplayName("ID로 식단을 조회한다")
    void findById() {

        // given
        Long id = 1L;


        /*
         * Repository의 findById() 반환 타입은 Optional<Meal>.
         *
         * Meal이 존재하는 상황을 만들기 위해
         * Optional.of(meal)을 반환하도록 설정한다.
         */
        when(mealRepository.findById(id))
                .thenReturn(Optional.of(meal));


        /*
         * when
         *
         * Service에 id=1을 전달한다.
         *
         * Service는 Repository에서 Meal을 조회한 후
         * MealResponse로 변환해서 반환해야 한다.
         */
        MealResponse response = mealService.findById(id);


        /*
         * then
         *
         * Entity의 모든 주요 값이
         * Response DTO로 정상 변환되었는지 확인한다.
         */
        assertEquals(
                "닭가슴살 샐러드",
                response.getMealName()
        );

        assertEquals(
                350,
                response.getCalories()
        );

        assertEquals(
                40,
                response.getProtein()
        );

        assertEquals(
                30,
                response.getCarbohydrate()
        );

        assertEquals(
                10,
                response.getFat()
        );


        /*
         * 전달받은 id가 Repository까지
         * 정확하게 전달되었는지 확인한다.
         */
        verify(mealRepository).findById(id);
    }


    /*
     * ============================================================
     * ID 단건 조회 - 실패
     * ============================================================
     */


    @Test
    @DisplayName("존재하지 않는 식단을 조회하면 예외가 발생한다")
    void findById_notFound() {

        // given
        Long id = 999L;


        /*
         * Repository에 해당 ID가 존재하지 않는 상황을 만든다.
         *
         * Spring Data JPA findById()는
         * 데이터가 없으면 null 대신 Optional.empty()를 반환한다.
         */
        when(mealRepository.findById(id))
                .thenReturn(Optional.empty());


        /*
         * when & then
         *
         * mealService.findById(id)를 호출했을 때
         * MealNotFoundException이 발생해야 한다.
         *
         *
         * assertThrows(
         *     기대하는 예외 타입,
         *     실제 실행할 코드
         * )
         *
         *
         * 즉 아래 테스트의 의미:
         *
         * "999번 Meal이 없으면
         *  MealService가 MealNotFoundException을 던지는가?"
         */
        assertThrows(
                MealNotFoundException.class,
                () -> mealService.findById(id)
        );


        /*
         * 예외가 발생하더라도
         * Repository 조회 자체는 수행되었어야 한다.
         */
        verify(mealRepository).findById(id);
    }


    /*
     * ============================================================
     * 등록
     * ============================================================
     */


    @Test
    @DisplayName("새로운 식단을 등록한다")
    void saveMeal() {

        /*
         * given
         *
         * Repository.save()가 호출되면
         * 전달받은 Meal 객체를 그대로 반환하도록 만든다.
         *
         *
         * 일반적인 thenReturn() 대신
         * thenAnswer()를 사용한 이유:
         *
         * save()에 어떤 Meal 객체가 들어올지
         * 테스트 작성 시점에는 직접 가지고 있지 않기 때문이다.
         *
         * MealService 내부에서 MealRequest를 이용해
         * 새로운 Meal 객체를 생성할 것이기 때문이다.
         *
         *
         * invocation.getArgument(0)
         *
         * → save() 메서드의 첫 번째 인자로 전달된 객체를 꺼낸다.
         *
         *
         * 결과적으로:
         *
         * repository.save(meal)
         *
         * 가 호출되면
         *
         * 그 meal 객체를 그대로 반환한다.
         */
        when(mealRepository.save(any(Meal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        /*
         * when
         *
         * Service에 MealRequest를 전달해
         * 등록 로직을 실행한다.
         *
         *
         * 예상 흐름:
         *
         * MealRequest
         *      ↓
         * Meal Entity 생성
         *      ↓
         * mealRepository.save()
         *      ↓
         * MealResponse 변환
         */
        MealResponse response = mealService.saveMeal(mealRequest);


        /*
         * then
         *
         * Request에 넣은 값이
         * Entity → Response를 거쳐
         * 정상적으로 유지됐는지 확인한다.
         */
        assertEquals(
                "닭가슴살 샐러드",
                response.getMealName()
        );

        assertEquals(
                350,
                response.getCalories()
        );

        assertEquals(
                40,
                response.getProtein()
        );

        assertEquals(
                30,
                response.getCarbohydrate()
        );

        assertEquals(
                10,
                response.getFat()
        );


        /*
         * Service가 실제로 Repository.save()를 호출했는지 확인.
         *
         * any(Meal.class)
         *
         * → 어떤 Meal 객체든 저장 요청이 발생했으면 된다.
         */
        verify(mealRepository)
                .save(any(Meal.class));
    }


    /*
     * ============================================================
     * 수정 - 성공
     * ============================================================
     */


    @Test
    @DisplayName("기존 식단을 수정한다")
    void updateMeal() {

        // given
        Long id = 1L;


        /*
         * 기존 Meal을 어떤 값으로 수정할지 나타내는 요청 DTO.
         */
        MealRequest updateRequest = new MealRequest();
        updateRequest.setMealName("연어 샐러드");
        updateRequest.setCalories(450);
        updateRequest.setProtein(35);
        updateRequest.setCarbohydrate(20);
        updateRequest.setFat(25);


        /*
         * id=1 Meal이 존재하는 상황을 만든다.
         */
        when(mealRepository.findById(id))
                .thenReturn(Optional.of(meal));


        /*
         * when
         *
         * Service의 수정 로직을 실행한다.
         *
         *
         * 예상 흐름:
         *
         * findById(id)
         *      ↓
         * 기존 Meal Entity 조회
         *      ↓
         * meal.update(...)
         *      ↓
         * Entity 상태 변경
         *      ↓
         * MealResponse 변환
         *
         *
         * 여기서 중요한 점:
         *
         * Service에 @Transactional이 적용되어 있다면
         * JPA Dirty Checking을 사용할 수 있기 때문에
         * update 후 Repository.save()를 다시 호출하지 않아도 된다.
         */
        MealResponse response =
                mealService.updateMeal(id, updateRequest);


        /*
         * then
         *
         * 기존 값이 수정 요청 값으로 변경되었는지 확인한다.
         */
        assertEquals(
                "연어 샐러드",
                response.getMealName()
        );

        assertEquals(
                450,
                response.getCalories()
        );

        assertEquals(
                35,
                response.getProtein()
        );

        assertEquals(
                20,
                response.getCarbohydrate()
        );

        assertEquals(
                25,
                response.getFat()
        );


        /*
         * 수정할 Entity를 찾기 위해
         * Repository.findById()가 호출되었는지 확인.
         */
        verify(mealRepository).findById(id);


        /*
         * 중요한 검증.
         *
         * updateMeal() 구현이 JPA Dirty Checking을 사용하는 구조라면
         * repository.save()를 다시 호출하지 않는 것이 정상이다.
         *
         *
         * never()
         *
         * → 해당 메서드가 한 번도 호출되지 않았음을 검증한다.
         *
         *
         * 만약 나중에 누군가 Service를 수정해서
         *
         * mealRepository.save(meal)
         *
         * 을 추가하면 이 테스트가 실패한다.
         *
         * 즉 현재 Service의 수정 전략까지 테스트로 고정하고 있다.
         */
        verify(
                mealRepository,
                never()
        ).save(any(Meal.class));
    }


    /*
     * ============================================================
     * 수정 - 존재하지 않는 Meal
     * ============================================================
     */


    @Test
    @DisplayName("존재하지 않는 식단을 수정하면 예외가 발생한다")
    void updateMeal_notFound() {

        // given
        Long id = 999L;


        /*
         * 수정 대상이 존재하지 않는 상황.
         */
        when(mealRepository.findById(id))
                .thenReturn(Optional.empty());


        /*
         * when & then
         *
         * 존재하지 않는 Meal을 수정하려 하면
         * MealNotFoundException이 발생해야 한다.
         */
        assertThrows(
                MealNotFoundException.class,
                () -> mealService.updateMeal(id, mealRequest)
        );


        /*
         * 수정하기 전에 조회는 수행되었어야 한다.
         */
        verify(mealRepository).findById(id);


        /*
         * Meal 자체를 찾지 못했기 때문에
         * save() 같은 저장 작업은 절대 수행되면 안 된다.
         */
        verify(
                mealRepository,
                never()
        ).save(any(Meal.class));
    }


    /*
     * ============================================================
     * 삭제 - 성공
     * ============================================================
     */


    @Test
    @DisplayName("식단을 삭제한다")
    void deleteMeal() {

        // given
        Long id = 1L;


        /*
         * 삭제할 Meal이 존재하는 상황을 만든다.
         *
         * Service가 먼저 findById(id)로 조회한다고 가정한다.
         */
        when(mealRepository.findById(id))
                .thenReturn(Optional.of(meal));


        /*
         * when
         *
         * 삭제 로직 실행.
         *
         *
         * 예상 흐름:
         *
         * findById(id)
         *      ↓
         * Meal 존재 확인
         *      ↓
         * repository.delete(meal)
         */
        mealService.deleteMeal(id);


        /*
         * then
         *
         * 삭제 전에 Meal 존재 여부를 조회했는지 확인.
         */
        verify(mealRepository).findById(id);


        /*
         * 조회된 정확한 Meal Entity를
         * Repository.delete()에 전달했는지 확인.
         */
        verify(mealRepository).delete(meal);
    }


    /*
     * ============================================================
     * 삭제 - 존재하지 않는 Meal
     * ============================================================
     */


    @Test
    @DisplayName("존재하지 않는 식단을 삭제하면 예외가 발생한다")
    void deleteMeal_notFound() {

        // given
        Long id = 999L;


        /*
         * 삭제하려는 Meal이 존재하지 않는 상황.
         */
        when(mealRepository.findById(id))
                .thenReturn(Optional.empty());


        /*
         * when & then
         *
         * 없는 데이터를 삭제하려 하면
         * MealNotFoundException이 발생해야 한다.
         */
        assertThrows(
                MealNotFoundException.class,
                () -> mealService.deleteMeal(id)
        );


        /*
         * 존재 확인을 위한 조회는 수행되어야 한다.
         */
        verify(mealRepository).findById(id);


        /*
         * 조회에 실패했으므로
         * Repository.delete()는 절대로 호출되면 안 된다.
         */
        verify(
                mealRepository,
                never()
        ).delete(any(Meal.class));
    }
}