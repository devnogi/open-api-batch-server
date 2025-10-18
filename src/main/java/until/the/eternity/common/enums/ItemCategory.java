package until.the.eternity.common.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemCategory {
    // 근거리 장비
    ONE_HANDED_WEAPON("한손 장비", "근거리 장비"),
    TWO_HANDED_WEAPON("양손 장비", "근거리 장비"),
    SWORD("검", "근거리 장비"),
    AXE("도끼", "근거리 장비"),
    BLUNT_WEAPON("둔기", "근거리 장비"),
    LANCE("랜스", "근거리 장비"),
    HANDLE("핸들", "근거리 장비"),
    KNUCKLE("너클", "근거리 장비"),
    CHAIN_BLADE("체인 블레이드", "근거리 장비"),

    // 원거리 장비
    BOW("활", "원거리 장비"),
    CROSSBOW("석궁", "원거리 장비"),
    DUAL_GUN("듀얼건", "원거리 장비"),
    SHURIKEN("수리검", "원거리 장비"),
    ATLATL("아틀라틀", "원거리 장비"),
    RANGED_CONSUMABLE("원거리 소모품", "원거리 장비"),

    // 마법 장비
    CYLINDER("실린더", "마법 장비"),
    STAFF("스태프", "마법 장비"),
    WAND("원드", "마법 장비"),
    MAGIC_BOOK("마도서", "마법 장비"),
    ORB("오브", "마법 장비"),

    // 갑옷 장비
    HEAVY_ARMOR("중갑옷", "갑옷 장비"),
    LIGHT_ARMOR("경갑옷", "갑옷 장비"),
    CLOTH("천옷", "갑옷 장비"),

    // 방어 장비
    GLOVES("장갑", "방어 장비"),
    SHOES("신발", "방어 장비"),
    HAT_WIG("모자/가발", "방어 장비"),
    SHIELD("방패", "방어 장비"),
    ROBE("로브", "방어 장비"),

    // 액세서리
    FACE_ACCESSORY("얼굴 장식", "액세서리"),
    ACCESSORY("액세서리", "액세서리"),
    WING("날개", "액세서리"),
    TAIL("꼬리", "액세서리"),

    // 특수 장비
    INSTRUMENT("악기", "특수 장비"),
    LIFE_TOOL("생활 도구", "특수 장비"),
    MARIONETTE("마리오네트", "특수 장비"),
    ECHOSTONE("에코스톤", "특수 장비"),
    EIDOS("에이도스", "특수 장비"),
    RELIC("유물", "특수 장비"),
    ETC_EQUIPMENT("기타 장비", "특수 장비"),

    // 설치물
    CHAIR_OBJECT("의자/사물", "설치물"),
    FARM_ISLAND("낭만농장/달빛섬", "설치물"),

    // 인챈트 용품
    ENCHANT_SCROLL("인챈트 스크롤", "인챈트 용품"),
    MAGIC_POWDER("마법가루", "인챈트 용품"),

    // 스크롤
    BLUEPRINT("도면", "스크롤"),
    CLOTH_PATTERN("옷본", "스크롤"),
    DEMON_SCROLL("마족 스크롤", "스크롤"),
    ETC_SCROLL("기타 스크롤", "스크롤"),

    // 마기그래피 용품
    MAGIGRAPH("마기그래프", "마기그래프 용품"),
    MAGIGRAPH_BLUEPRINT("마기그래프 도안", "마기그래프 용품"),
    ETC_MATERIAL("기타 재료", "마기그래프 용품"),

    // 서적
    BOOK("책", "서적"),
    MABINOVEL("마비노벨", "서적"),
    PAGE("페이지", "서적"),

    // 소모품
    POTION("포션", "소모품"),
    FOOD("음식", "소모품"),
    HERB("허브", "소모품"),
    DUNGEON_PASS("던전 통행증", "소모품"),
    ALBAN_TRAINING_STONE("알반 훈련석", "소모품"),
    GEM_STONE("개조석", "소모품"),
    JEWEL("보석", "소모품"),
    TRANSFORMATION_MEDAL("변신 메달", "소모품"),
    DYE_AMPULE("염색 앰플", "소모품"),
    SKETCH("스케치", "소모품"),
    FINZBEADS("핀즈비즈", "소모품"),
    ETC_CONSUMABLE("기타 소모품", "소모품"),

    // 토템
    TOTEM("토템", "토템"),

    // 생활 재료
    POUCH("주머니", "생활 재료"),
    CLOTH_WEAVING("천옷/방직", "생활 재료"),
    REFINING_BLACKSMITH("제련/블랙스미스", "생활 재료"),
    HILLWEN_ENGINEERING("힐웬 공학", "생활 재료"),
    MAGIC_CRAFT("매직 크래프트", "생활 재료"),

    // 기타
    GESTURE("제스처", "기타"),
    SPEECH_BUBBLE_STICKER("말풍선 스티커", "기타"),
    FINI_PET("피니 펫", "기타"),
    FIREBALL("불타래", "기타"),
    PERFUME("퍼퓸", "기타"),
    ADOPTION_MEDAL("분양 메달", "기타"),
    BEAUTY_COUPON("뷰티 쿠폰", "기타"),
    ETC("기타", "기타");

    private final String subCategory;
    private final String topCategory;

    /** 성능을 위해 모든 enum 값을 <subCategory, ItemCategory> 매핑으로 캐싱해 둡니다. */
    private static final Map<String, ItemCategory> CACHE =
            Arrays.stream(values())
                    .collect(Collectors.toMap(ItemCategory::getSubCategory, Function.identity()));

    /**
     * 주어진 하위 카테고리(subCategory)에 대응하는 상위 카테고리(topCategory)를 반환합니다.
     *
     * @param subCategory 하위 카테고리(예: "검", "포션" 등)
     * @return 상위 카테고리(예: "근거리 장비", "소모품" 등)
     * @throws IllegalArgumentException 존재하지 않는 하위 카테고리인 경우
     */
    public static String findTopCategory(String subCategory) {
        ItemCategory itemCategory = CACHE.get(subCategory);
        if (itemCategory == null) {
            throw new IllegalArgumentException("Unknown subCategory: " + subCategory);
        }
        return itemCategory.getTopCategory();
    }

    public static ItemCategory findBySubCategory(String subCategory) {
        return CACHE.get(subCategory);
    }
}
