package until.the.eternity.auctionhistory.infrastructure.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.domain.entity.QAuctionHistory;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.*;
import until.the.eternity.auctionitemoption.domain.entity.QAuctionItemOption;

@Component
@RequiredArgsConstructor
class AuctionHistoryQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 경매 거래내역 검색 (옵션 조건 포함)
     *
     * <p>검색 흐름: 1. 옵션 조건을 만족하는 거래내역 ID를 서브쿼리로 찾기 2. 거래내역 조건으로 필터링 3. 해당 거래내역의 모든 옵션을 함께 조회 (LEFT
     * JOIN)
     */
    public Page<AuctionHistory> search(AuctionHistorySearchRequest condition, Pageable pageable) {
        QAuctionHistory ah = QAuctionHistory.auctionHistory;
        QAuctionItemOption aio = QAuctionItemOption.auctionItemOption;

        // 1단계: 거래내역 조건 빌드
        BooleanBuilder historyBuilder = buildHistoryPredicate(condition, ah);

        // 2단계: 옵션 조건이 있으면 서브쿼리 추가
        if (condition.itemOptionSearchRequest() != null) {
            // 서브쿼리용 별도 QAuctionItemOption 인스턴스
            QAuctionItemOption subOption = new QAuctionItemOption("subOption");
            BooleanBuilder optionBuilder =
                    buildItemOptionConditions(condition.itemOptionSearchRequest(), subOption);

            // 옵션 조건이 실제로 있는 경우에만 서브쿼리 추가
            if (optionBuilder.hasValue()) {
                // 서브쿼리: 옵션 조건을 만족하는 auction_history_id 찾기
                var subQuery =
                        JPAExpressions.select(subOption.auctionHistory.auctionBuyId)
                                .from(subOption)
                                .where(optionBuilder)
                                .distinct();

                // 메인 쿼리에 서브쿼리 결과 적용
                historyBuilder.and(ah.auctionBuyId.in(subQuery));
            }
        }

        // 3단계: 모든 옵션과 함께 조회 (LEFT JOIN - 조건 없음!)
        List<AuctionHistory> content =
                queryFactory
                        .selectFrom(ah)
                        .leftJoin(ah.auctionItemOptions, aio)
                        .fetchJoin()
                        .where(historyBuilder)
                        .distinct() // 중복 제거
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        // Count 쿼리 (JOIN 없이 실행)
        Long total =
                queryFactory.select(ah.countDistinct()).from(ah).where(historyBuilder).fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    /** 거래내역 기본 조건 빌드 (카테고리, 아이템명, 가격, 거래일자) */
    private BooleanBuilder buildHistoryPredicate(
            AuctionHistorySearchRequest c, QAuctionHistory ah) {
        BooleanBuilder builder = new BooleanBuilder();

        // 기본 조건들
        if (c.itemTopCategory() != null && !c.itemTopCategory().isBlank()) {
            builder.and(ah.itemTopCategory.eq(c.itemTopCategory()));
        }
        if (c.itemSubCategory() != null && !c.itemSubCategory().isBlank()) {
            builder.and(ah.itemSubCategory.eq(c.itemSubCategory()));
        }
        if (c.itemName() != null && !c.itemName().isBlank()) {
            builder.and(ah.itemName.containsIgnoreCase(c.itemName()));
        }

        // 가격 조건 (PriceSearchRequest가 있으면)
        if (c.priceSearchRequest() != null) {
            PriceSearchRequest price = c.priceSearchRequest();
            if (price.priceFrom() != null) {
                builder.and(ah.auctionPricePerUnit.goe(price.priceFrom()));
            }
            if (price.priceTo() != null) {
                builder.and(ah.auctionPricePerUnit.loe(price.priceTo()));
            }
        }

        // 거래 일자 조건
        if (c.date_auction_buy() != null && !c.date_auction_buy().isBlank()) {
            // 날짜 파싱 및 조건 추가 로직
            // TODO: 날짜 범위 검색 구현
        }

        return builder;
    }

    /**
     * 옵션 검색 조건 빌드 (서브쿼리용)
     *
     * <p>주의: 이 메서드는 서브쿼리에서만 사용됩니다. 반환된 BooleanBuilder는 메인 JOIN의 WHERE에 직접 사용하면 안 됩니다!
     */
    private BooleanBuilder buildItemOptionConditions(
            ItemOptionSearchRequest opt, QAuctionItemOption aio) {
        BooleanBuilder builder = new BooleanBuilder();

        // 1. Balance (밸런스)
        if (opt.balanceSearch() != null && opt.balanceSearch().balance() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "밸런스",
                            opt.balanceSearch().balance(),
                            opt.balanceSearch().balanceStandard()));
        }

        // 2. Critical (크리티컬)
        if (opt.criticalSearch() != null && opt.criticalSearch().critical() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "크리티컬",
                            opt.criticalSearch().critical(),
                            opt.criticalSearch().criticalStandard()));
        }

        // 3. Defense (방어력)
        if (opt.defenseSearch() != null && opt.defenseSearch().defense() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "방어력",
                            opt.defenseSearch().defense(),
                            opt.defenseSearch().defenseStandard()));
        }

        // 4. Erg (에르그) - 범위 검색
        if (opt.ergSearch() != null) {
            BooleanBuilder ergBuilder = new BooleanBuilder(aio.optionType.eq("에르그"));
            if (opt.ergSearch().ergFrom() != null && opt.ergSearch().ergTo() != null) {
                ergBuilder.and(
                        castOptionValueToInt(aio)
                                .between(opt.ergSearch().ergFrom(), opt.ergSearch().ergTo()));
            } else if (opt.ergSearch().ergFrom() != null) {
                ergBuilder.and(castOptionValueToInt(aio).goe(opt.ergSearch().ergFrom()));
            } else if (opt.ergSearch().ergTo() != null) {
                ergBuilder.and(castOptionValueToInt(aio).loe(opt.ergSearch().ergTo()));
            }
            if (ergBuilder.hasValue()) {
                builder.or(ergBuilder);
            }
        }

        // 5. ErgRank (에르그 등급) - 문자열 비교
        if (opt.ergRankSearch() != null && opt.ergRankSearch().ergRank() != null) {
            builder.or(
                    aio.optionType
                            .eq("에르그")
                            .and(aio.optionValue.eq(opt.ergRankSearch().ergRank())));
        }

        // 6. MagicDefense (마법 방어력)
        if (opt.magicDefenseSearch() != null && opt.magicDefenseSearch().magicDefense() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "마법 방어력",
                            opt.magicDefenseSearch().magicDefense(),
                            opt.magicDefenseSearch().magicDefenseStandard()));
        }

        // 7. MagicProtect (마법 보호)
        if (opt.magicProtectSearch() != null && opt.magicProtectSearch().magicProtect() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "마법 보호",
                            opt.magicProtectSearch().magicProtect(),
                            opt.magicProtectSearch().magicProtectStandard()));
        }

        // 8. MaxAttack (공격) - 범위 검색
        if (opt.maxAttackSearch() != null) {
            BooleanBuilder attackBuilder = new BooleanBuilder(aio.optionType.eq("공격"));
            if (opt.maxAttackSearch().maxAttackFrom() != null
                    && opt.maxAttackSearch().maxAttackTo() != null) {
                attackBuilder.and(
                        castOptionValueToInt(aio)
                                .between(
                                        opt.maxAttackSearch().maxAttackFrom(),
                                        opt.maxAttackSearch().maxAttackTo()));
            } else if (opt.maxAttackSearch().maxAttackFrom() != null) {
                attackBuilder.and(
                        castOptionValueToInt(aio).goe(opt.maxAttackSearch().maxAttackFrom()));
            } else if (opt.maxAttackSearch().maxAttackTo() != null) {
                attackBuilder.and(
                        castOptionValueToInt(aio).loe(opt.maxAttackSearch().maxAttackTo()));
            }
            if (attackBuilder.hasValue()) {
                builder.or(attackBuilder);
            }
        }

        // 9. MaximumDurability (내구력)
        if (opt.maximumDurabilitySearch() != null
                && opt.maximumDurabilitySearch().maximumDurability() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "내구력",
                            opt.maximumDurabilitySearch().maximumDurability(),
                            opt.maximumDurabilitySearch().maximumDurabilityStandard()));
        }

        // 10. MaxInjuryRate (부상률) - 범위 검색
        if (opt.maxInjuryRateSearch() != null) {
            BooleanBuilder injuryBuilder = new BooleanBuilder(aio.optionType.eq("부상률"));
            if (opt.maxInjuryRateSearch().maxInjuryRateFrom() != null
                    && opt.maxInjuryRateSearch().maxInjuryRateTo() != null) {
                injuryBuilder.and(
                        castOptionValueToInt(aio)
                                .between(
                                        opt.maxInjuryRateSearch().maxInjuryRateFrom(),
                                        opt.maxInjuryRateSearch().maxInjuryRateTo()));
            } else if (opt.maxInjuryRateSearch().maxInjuryRateFrom() != null) {
                injuryBuilder.and(
                        castOptionValueToInt(aio)
                                .goe(opt.maxInjuryRateSearch().maxInjuryRateFrom()));
            } else if (opt.maxInjuryRateSearch().maxInjuryRateTo() != null) {
                injuryBuilder.and(
                        castOptionValueToInt(aio).loe(opt.maxInjuryRateSearch().maxInjuryRateTo()));
            }
            if (injuryBuilder.hasValue()) {
                builder.or(injuryBuilder);
            }
        }

        // 11. Proficiency (숙련)
        if (opt.proficiencySearch() != null && opt.proficiencySearch().proficiency() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "숙련",
                            opt.proficiencySearch().proficiency(),
                            opt.proficiencySearch().proficiencyStandard()));
        }

        // 12. Protect (보호)
        if (opt.protectSearch() != null && opt.protectSearch().protect() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "보호",
                            opt.protectSearch().protect(),
                            opt.protectSearch().protectStandard()));
        }

        // 13. RemainingTransactionCount (남은 거래 횟수)
        if (opt.remainingTransactionCountSearch() != null
                && opt.remainingTransactionCountSearch().remainingTransactionCount() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "남은 거래 횟수",
                            opt.remainingTransactionCountSearch().remainingTransactionCount(),
                            opt.remainingTransactionCountSearch()
                                    .remainingTransactionCountStandard()));
        }

        // 14. RemainingUnsealCount (남은 전용 해제 가능 횟수)
        if (opt.remainingUnsealCountSearch() != null
                && opt.remainingUnsealCountSearch().remainingUnsealCount() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "남은 전용 해제 가능 횟수",
                            opt.remainingUnsealCountSearch().remainingUnsealCount(),
                            opt.remainingUnsealCountSearch().remainingUnsealCountStandard()));
        }

        // 15. RemainingUseCount (남은 사용 횟수)
        if (opt.remainingUseCountSearch() != null
                && opt.remainingUseCountSearch().remainingUseCount() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "남은 사용 횟수",
                            opt.remainingUseCountSearch().remainingUseCount(),
                            opt.remainingUseCountSearch().remainingUseCountStandard()));
        }

        // 16. WearingRestrictions (착용 제한) - 문자열 비교
        if (opt.wearingRestrictionsSearch() != null
                && opt.wearingRestrictionsSearch().wearingRestrictions() != null) {
            builder.or(
                    aio.optionValue.contains(
                            opt.wearingRestrictionsSearch().wearingRestrictions()));
        }

        return builder;
    }

    /**
     * 옵션 조건 빌드 헬퍼 (option_type + 숫자 비교 + UP/DOWN)
     *
     * @param aio QueryDSL Q타입
     * @param optionType DB의 option_type 값 (예: "밸런스", "크리티컬")
     * @param value 비교할 숫자 값
     * @param standard UP(이상) / DOWN(이하) / null(같음)
     */
    private BooleanBuilder buildOptionCondition(
            QAuctionItemOption aio, String optionType, Integer value, String standard) {
        BooleanBuilder condition = new BooleanBuilder(aio.optionType.eq(optionType));

        NumberTemplate<Integer> numValue = castOptionValueToInt(aio);

        if ("UP".equals(standard)) {
            condition.and(numValue.goe(value)); // 이상 (>=)
        } else if ("DOWN".equals(standard)) {
            condition.and(numValue.loe(value)); // 이하 (<=)
        } else {
            condition.and(numValue.eq(value)); // 같음
        }

        return condition;
    }

    /**
     * option_value2 또는 option_value를 Integer로 변환하는 NumberTemplate
     *
     * <p>COALESCE를 사용하여 null 처리 후 숫자로 비교 (MySQL은 자동 타입 변환 수행)
     */
    private NumberTemplate<Integer> castOptionValueToInt(QAuctionItemOption aio) {
        return Expressions.numberTemplate(
                Integer.class, "COALESCE({0}, {1}, 0)", aio.optionValue2, aio.optionValue);
    }
}
