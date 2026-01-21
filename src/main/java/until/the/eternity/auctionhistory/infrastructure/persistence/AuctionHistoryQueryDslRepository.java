package until.the.eternity.auctionhistory.infrastructure.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.domain.entity.AuctionHistory;
import until.the.eternity.auctionhistory.domain.entity.QAuctionHistory;
import until.the.eternity.auctionhistory.interfaces.rest.dto.enums.SearchStandard;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.AuctionHistorySearchRequest;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.DateAuctionBuyRequest;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.ItemOptionSearchRequest;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.PriceSearchRequest;
import until.the.eternity.auctionitemoption.domain.entity.QAuctionItemOption;

@Component
@RequiredArgsConstructor
class AuctionHistoryQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /** 옵션 조건 빌드 결과 (조건 BooleanBuilder + 추가된 조건 개수) */
    record OptionConditionResult(BooleanBuilder builder, int count) {}

    /** 경매 거래내역 검색 (옵션 조건 포함) */
    public Page<AuctionHistory> search(AuctionHistorySearchRequest condition, Pageable pageable) {
        QAuctionHistory ah = QAuctionHistory.auctionHistory;
        QAuctionItemOption aio = QAuctionItemOption.auctionItemOption;

        // 1단계: 거래내역 조건 빌드
        BooleanBuilder historyBuilder = buildHistoryPredicate(condition, ah);

        // 2단계: 옵션 조건이 있으면 서브쿼리 추가
        if (condition.itemOptionSearchRequest() != null) {
            // 서브쿼리용 별도 QAuctionItemOption 인스턴스
            QAuctionItemOption subOption = new QAuctionItemOption("subOption");
            OptionConditionResult optionResult =
                    buildItemOptionConditions(condition.itemOptionSearchRequest(), subOption);

            // 옵션 조건이 실제로 있는 경우에만 서브쿼리 추가
            if (optionResult.builder().hasValue() && optionResult.count() > 0) {
                // 서브쿼리: 옵션 조건을 만족하는 auction_history_id 찾기
                // GROUP BY + HAVING COUNT로 모든 조건을 만족하는 거래내역만 필터링
                var subQuery =
                        JPAExpressions.select(subOption.auctionHistory.auctionBuyId)
                                .from(subOption)
                                .where(optionResult.builder())
                                .groupBy(subOption.auctionHistory.auctionBuyId)
                                .having(subOption.count().eq((long) optionResult.count()));

                // 메인 쿼리에 서브쿼리 결과 적용
                historyBuilder.and(ah.auctionBuyId.in(subQuery));
            }
        }

        // 3단계: 정렬 조건 빌드
        List<OrderSpecifier<?>> orderSpecifiers = buildOrderSpecifiers(pageable, ah);

        // 4단계: Deferred Join (Late Row Lookup) 패턴 적용
        // 4-1단계: ID만 먼저 조회 (인덱스 활용)
        List<String> ids =
                queryFactory
                        .select(ah.auctionBuyId)
                        .from(ah)
                        .where(historyBuilder)
                        .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        // 결과가 없으면 빈 페이지 반환
        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0L);
        }

        // 4-2단계: ID로 상세 조회 (LEFT JOIN으로 옵션 포함)
        List<AuctionHistory> content =
                queryFactory
                        .selectFrom(ah)
                        .leftJoin(ah.auctionItemOptions, aio)
                        .fetchJoin()
                        .where(ah.auctionBuyId.in(ids))
                        .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                        .distinct()
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

        // 거래 일자 조건 (String 'yyyy-MM-dd' → Instant 변환)
        if (c.dateAuctionBuyRequest() != null) {
            DateAuctionBuyRequest date = c.dateAuctionBuyRequest();
            if (date.dateAuctionBuyFrom() != null && !date.dateAuctionBuyFrom().isBlank()) {
                Instant fromInstant =
                        LocalDate.parse(date.dateAuctionBuyFrom())
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant();
                builder.and(ah.dateAuctionBuy.goe(fromInstant));
            }
            if (date.dateAuctionBuyTo() != null && !date.dateAuctionBuyTo().isBlank()) {
                Instant toInstant =
                        LocalDate.parse(date.dateAuctionBuyTo())
                                .plusDays(1)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant();
                builder.and(ah.dateAuctionBuy.lt(toInstant));
            }
        }

        return builder;
    }

    /** 옵션 검색 조건 빌드 (서브쿼리용) */
    private OptionConditionResult buildItemOptionConditions(
            ItemOptionSearchRequest opt, QAuctionItemOption aio) {
        BooleanBuilder builder = new BooleanBuilder();
        int conditionCount = 0;
        boolean ergConditionAdded = false; // 에르그 조건 추가 여부 (레벨/랭크 통합)

        // 1. Balance (밸런스)
        if (opt.balanceSearch() != null && opt.balanceSearch().balance() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "밸런스",
                            opt.balanceSearch().balance(),
                            opt.balanceSearch().balanceStandard()));
            conditionCount++;
        }

        // 2. Critical (크리티컬)
        if (opt.criticalSearch() != null && opt.criticalSearch().critical() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "크리티컬",
                            opt.criticalSearch().critical(),
                            opt.criticalSearch().criticalStandard()));
            conditionCount++;
        }

        // 3. Defense (방어력)
        if (opt.defenseSearch() != null && opt.defenseSearch().defense() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "방어력",
                            opt.defenseSearch().defense(),
                            opt.defenseSearch().defenseStandard()));
            conditionCount++;
        }

        // 4. Erg (에르그) - 범위 검색
        if (opt.ergSearch() != null) {
            BooleanExpression ergTypeCondition = aio.optionType.eq("에르그");
            BooleanExpression ergValueCondition = null;

            if (opt.ergSearch().ergFrom() != null && opt.ergSearch().ergTo() != null) {
                ergValueCondition =
                        castOptionValueToInt(aio)
                                .between(opt.ergSearch().ergFrom(), opt.ergSearch().ergTo());
            } else if (opt.ergSearch().ergFrom() != null) {
                ergValueCondition = castOptionValueToInt(aio).goe(opt.ergSearch().ergFrom());
            } else if (opt.ergSearch().ergTo() != null) {
                ergValueCondition = castOptionValueToInt(aio).loe(opt.ergSearch().ergTo());
            }

            if (ergValueCondition != null) {
                // 명시적으로 괄호를 추가
                BooleanExpression combined = ergTypeCondition.and(ergValueCondition);
                builder.or(Expressions.booleanTemplate("({0})", combined));
                // 에르그는 레벨/랭크 통합하여 1개로 카운트
                if (!ergConditionAdded) {
                    conditionCount++;
                    ergConditionAdded = true;
                }
            }
        }

        // 5. ErgRank (에르그 등급) - 문자열 비교
        if (opt.ergRankSearch() != null && opt.ergRankSearch().ergRank() != null) {
            BooleanExpression combined =
                    aio.optionType.eq("에르그").and(aio.optionValue.eq(opt.ergRankSearch().ergRank()));
            builder.or(Expressions.booleanTemplate("({0})", combined));
            // 에르그는 레벨/랭크 통합하여 1개로 카운트
            if (!ergConditionAdded) {
                conditionCount++;
                ergConditionAdded = true;
            }
        }

        // 6. MagicDefense (마법 방어력)
        if (opt.magicDefenseSearch() != null && opt.magicDefenseSearch().magicDefense() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "마법 방어력",
                            opt.magicDefenseSearch().magicDefense(),
                            opt.magicDefenseSearch().magicDefenseStandard()));
            conditionCount++;
        }

        // 7. MagicProtect (마법 보호)
        if (opt.magicProtectSearch() != null && opt.magicProtectSearch().magicProtect() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "마법 보호",
                            opt.magicProtectSearch().magicProtect(),
                            opt.magicProtectSearch().magicProtectStandard()));
            conditionCount++;
        }

        // 8. MaxAttack (공격) - 범위 검색
        if (opt.maxAttackSearch() != null) {
            BooleanExpression attackTypeCondition = aio.optionType.eq("공격");
            BooleanExpression attackValueCondition = null;

            if (opt.maxAttackSearch().maxAttackFrom() != null
                    && opt.maxAttackSearch().maxAttackTo() != null) {
                attackValueCondition =
                        castOptionValueToInt(aio)
                                .between(
                                        opt.maxAttackSearch().maxAttackFrom(),
                                        opt.maxAttackSearch().maxAttackTo());
            } else if (opt.maxAttackSearch().maxAttackFrom() != null) {
                attackValueCondition =
                        castOptionValueToInt(aio).goe(opt.maxAttackSearch().maxAttackFrom());
            } else if (opt.maxAttackSearch().maxAttackTo() != null) {
                attackValueCondition =
                        castOptionValueToInt(aio).loe(opt.maxAttackSearch().maxAttackTo());
            }

            if (attackValueCondition != null) {
                // 명시적으로 괄호를 추가
                BooleanExpression combined = attackTypeCondition.and(attackValueCondition);
                builder.or(Expressions.booleanTemplate("({0})", combined));
                conditionCount++;
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
            conditionCount++;
        }

        // 10. MaxInjuryRate (부상률) - 범위 검색
        if (opt.maxInjuryRateSearch() != null) {
            BooleanExpression injuryTypeCondition = aio.optionType.eq("부상률");
            BooleanExpression injuryValueCondition = null;

            if (opt.maxInjuryRateSearch().maxInjuryRateFrom() != null
                    && opt.maxInjuryRateSearch().maxInjuryRateTo() != null) {
                injuryValueCondition =
                        castOptionValueToInt(aio)
                                .between(
                                        opt.maxInjuryRateSearch().maxInjuryRateFrom(),
                                        opt.maxInjuryRateSearch().maxInjuryRateTo());
            } else if (opt.maxInjuryRateSearch().maxInjuryRateFrom() != null) {
                injuryValueCondition =
                        castOptionValueToInt(aio)
                                .goe(opt.maxInjuryRateSearch().maxInjuryRateFrom());
            } else if (opt.maxInjuryRateSearch().maxInjuryRateTo() != null) {
                injuryValueCondition =
                        castOptionValueToInt(aio).loe(opt.maxInjuryRateSearch().maxInjuryRateTo());
            }

            if (injuryValueCondition != null) {
                // 명시적으로 괄호를 추가
                BooleanExpression combined = injuryTypeCondition.and(injuryValueCondition);
                builder.or(Expressions.booleanTemplate("({0})", combined));
                conditionCount++;
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
            conditionCount++;
        }

        // 12. Protect (보호)
        if (opt.protectSearch() != null && opt.protectSearch().protect() != null) {
            builder.or(
                    buildOptionCondition(
                            aio,
                            "보호",
                            opt.protectSearch().protect(),
                            opt.protectSearch().protectStandard()));
            conditionCount++;
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
            conditionCount++;
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
            conditionCount++;
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
            conditionCount++;
        }

        // 16. WearingRestrictions (착용 제한) - 문자열 비교
        if (opt.wearingRestrictionsSearch() != null
                && opt.wearingRestrictionsSearch().wearingRestrictions() != null) {
            BooleanExpression condition =
                    aio.optionValue.contains(opt.wearingRestrictionsSearch().wearingRestrictions());
            builder.or(Expressions.booleanTemplate("({0})", condition));
            conditionCount++;
        }

        return new OptionConditionResult(builder, conditionCount);
    }

    /** 옵션 조건 빌드 헬퍼 (option_type + 숫자 비교 + SearchStandard) */
    private BooleanExpression buildOptionCondition(
            QAuctionItemOption aio, String optionType, Integer value, SearchStandard standard) {
        BooleanExpression optionTypeCondition = aio.optionType.eq(optionType);

        NumberTemplate<Integer> numValue = castOptionValueToInt(aio);

        BooleanExpression valueCondition;
        if (standard == null || standard.isEqual()) {
            valueCondition = numValue.eq(value); // 같음
        } else if (standard.isUp()) {
            valueCondition = numValue.goe(value); // 이상 (>=)
        } else if (standard.isDown()) {
            valueCondition = numValue.loe(value); // 이하 (<=)
        } else {
            valueCondition = numValue.eq(value); // 기본값: 같음
        }

        // 명시적 괄호 추가
        BooleanExpression combined = optionTypeCondition.and(valueCondition);
        return Expressions.booleanTemplate("({0})", combined);
    }

    /** option_value2 또는 option_value를 Integer로 변환하는 NumberTemplate */
    private NumberTemplate<Integer> castOptionValueToInt(QAuctionItemOption aio) {
        return Expressions.numberTemplate(
                Integer.class, "COALESCE({0}, {1}, 0)", aio.optionValue2, aio.optionValue);
    }

    /** Pageable의 Sort를 QueryDSL OrderSpecifier로 변환 */
    private List<OrderSpecifier<?>> buildOrderSpecifiers(Pageable pageable, QAuctionHistory ah) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                String property = order.getProperty();

                // 필드명에 따라 QueryDSL 정렬 표현식 생성
                OrderSpecifier<?> orderSpecifier =
                        switch (property) {
                            case "dateAuctionBuy" ->
                                    new OrderSpecifier<>(direction, ah.dateAuctionBuy);
                            case "auctionPricePerUnit" ->
                                    new OrderSpecifier<>(direction, ah.auctionPricePerUnit);
                            case "itemName" -> new OrderSpecifier<>(direction, ah.itemName);
                            default -> new OrderSpecifier<>(Order.DESC, ah.dateAuctionBuy); // 기본값
                        };

                orders.add(orderSpecifier);
            }
        } else {
            // 정렬 조건이 없으면 기본 정렬은 최신 거래일자순: dateAuctionBuy DESC
            orders.add(new OrderSpecifier<>(Order.DESC, ah.dateAuctionBuy));
        }

        return orders;
    }
}
