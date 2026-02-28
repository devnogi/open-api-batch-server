package until.the.eternity.config;

/** Redis 캐시 이름 상수 클래스. */
public final class CacheNames {

    private CacheNames() {}

    // ===== 랭킹 =====
    public static final String RANKING_PRICE_TODAY_HIGHEST = "ranking:price:today:highest";
    public static final String RANKING_PRICE_WEEK_HIGHEST = "ranking:price:week:highest";
    public static final String RANKING_PRICE_TODAY_VOLUME = "ranking:price:today:volume";
    public static final String RANKING_VOLUME_TODAY_POPULAR = "ranking:volume:today:popular";
    public static final String RANKING_VOLUME_WEEK_POPULAR = "ranking:volume:week:popular";
    public static final String RANKING_CHANGE_PRICE_SURGE = "ranking:change:price:surge";
    public static final String RANKING_CHANGE_PRICE_DROP = "ranking:change:price:drop";
    public static final String RANKING_CHANGE_VOLUME_SURGE = "ranking:change:volume:surge";
    public static final String RANKING_CATEGORY_HIGHEST = "ranking:category:highest";
    public static final String RANKING_CATEGORY_POPULAR = "ranking:category:popular";
    public static final String RANKING_ALLTIME_HIGHEST = "ranking:alltime:highest";
    public static final String RANKING_ALLTIME_MONTH_VOLUME = "ranking:alltime:month:volume";

    // ===== 검색 옵션 메타데이터 =====
    public static final String SEARCH_OPTION_ALL_ACTIVE = "search-option:all-active";

    // ===== 아이템 마스터 =====
    public static final String ITEM_INFO_ALL = "item-info:all";
    public static final String ITEM_INFO_BY_TOP_CATEGORY = "item-info:by-top-category";
    public static final String ITEM_INFO_BY_SUB_CATEGORY = "item-info:by-sub-category";
    public static final String ITEM_INFO_DETAIL = "item-info:detail";
    public static final String ITEM_INFO_SUMMARY = "item-info:summary";

    // ===== 인챈트 마스터 =====
    public static final String ENCHANT_INFO_ALL = "enchant-info:all";
    public static final String ENCHANT_INFO_FULLNAMES = "enchant-info:fullnames";

    // ===== 세공 마스터 =====
    public static final String METALWARE_INFO_ALL = "metalware-info:all";
    public static final String METALWARE_ATTRIBUTE_INFO_SEARCH = "metalware-attribute-info:search";

    // ===== 통계 (일간) =====
    public static final String STATISTICS_ITEM_DAILY = "statistics:item:daily";
    public static final String STATISTICS_SUBCATEGORY_DAILY = "statistics:subcategory:daily";
    public static final String STATISTICS_TOPCATEGORY_DAILY = "statistics:topcategory:daily";

    // ===== 통계 (주간) =====
    public static final String STATISTICS_ITEM_WEEKLY = "statistics:item:weekly";
    public static final String STATISTICS_SUBCATEGORY_WEEKLY = "statistics:subcategory:weekly";
    public static final String STATISTICS_TOPCATEGORY_WEEKLY = "statistics:topcategory:weekly";

    // ===== 실시간 경매 =====
    public static final String AUCTION_REALTIME_SEARCH = "auction-realtime:search";

    // ===== 경매 거래 내역 =====
    public static final String AUCTION_HISTORY_SEARCH = "auction-history:search";
}
