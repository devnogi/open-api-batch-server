package until.the.eternity.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemCategory {
    // 근거리 장비
    ONE_HANDED_WEAPON("한손 장비"),
    TWO_HANDED_WEAPON("양손 장비"),
    SWORD("검"),
    AXE("도끼"),
    BLUNT_WEAPON("둔기"),
    LANCE("랜스"),
    HANDLE("핸들"),
    KNUCKLE("너클"),
    CHAIN_BLADE("체인 블레이드"),
    // 원거리 장비
    SHURIKEN("수리검"),
    DUAL_GUN("듀얼건"),
    BOW("활"),
    CROSSBOW("석궁"),
    RANGED_CONSUMABLE("원거리 소모품"),
    ATLATL("아틀라틀"),
    // 마법 장비
    MAGIC_BOOK("마도서"),
    STAFF("스태프"),
    CYLINDER("실린더"),
    // 갑옷 장비
    // 방어 장비
    // 액세서리
    // 특수 장비
    // 설치물
    // 인챈트 용품
    // 스크롤
    // 마기그래피 용품
    // 서적
    // 소모품
    // 토템
    // 생활 재료
    // 기타
    GEM_STONE("개조석"),

    LIGHT_ARMOR("경갑옷"),
    ETC("기타"),
    ETC_CONSUMABLE("기타 소모품"),
    ETC_SCROLL("기타 스크롤"),
    ETC_EQUIPMENT("기타 장비"),
    ETC_MATERIAL("기타 재료"),
    TAIL("꼬리"),
    WING("날개"),
    FARM_ISLAND("낭만농장/달빛섬"),

    DUNGEON_PASS("던전 통행증"),

    BLUEPRINT("도면"),

    ROBE("로브"),
    MAGIGRAPH("마기그래프"),
    MAGIGRAPH_BLUEPRINT("마기그래프 도안"),

    MARIONETTE("마리오네트"),
    MAGIC_POWDER("마법가루"),
    MABINOVEL("마비노벨"),
    DEMON_SCROLL("마족 스크롤"),
    SPEECH_BUBBLE_STICKER("말풍선 스티커"),
    MAGIC_CRAFT("매직 크래프트"),
    HAT_WIG("모자/가발"),
    SHIELD("방패"),
    TRANSFORMATION_MEDAL("변신 메달"),
    JEWEL("보석"),
    ADOPTION_MEDAL("분양 메달"),
    FIREBALL("불타래"),
    BEAUTY_COUPON("뷰티 쿠폰"),
    LIFE_TOOL("생활 도구"),

    SKETCH("스케치"),

    SHOES("신발"),

    INSTRUMENT("악기"),
    ALBAN_TRAINING_STONE("알반 훈련석"),
    ACCESSORY("액세서리"),

    FACE_ACCESSORY("얼굴 장식"),
    EIDOS("에이도스"),
    ECHOSTONE("에코스톤"),
    DYE_AMPULE("염색 앰플"),
    ORB("오브"),
    CLOTH_PATTERN("옷본"),

    WAND("원드"),
    FOOD("음식"),
    CHAIR_OBJECT("의자/사물"),
    ENCHANT_SCROLL("인챈트 스크롤"),
    GLOVES("장갑"),
    REFINING_BLACKSMITH("제련/블랙스미스"),
    GESTURE("제스처"),
    POUCH("주머니"),
    HEAVY_ARMOR("중갑옷"),
    BOOK("책"),
    CLOTH("천옷"),
    CLOTH_WEAVING("천옷/방직"),

    TOTEM("토템"),
    PALLIASH_RELIC("팔리아스 유물"),
    PERFUME("퍼퓸"),
    PAGE("페이지"),
    POTION("포션"),
    FINI_PET("피니 펫"),
    FINZBEADS("핀즈비즈"),

    HERB("허브"),

    HILLWEN_ENGINEERING("힐웬 공학");

    private final String itemName;
}
