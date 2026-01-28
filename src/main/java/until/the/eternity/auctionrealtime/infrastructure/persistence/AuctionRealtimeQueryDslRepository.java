package until.the.eternity.auctionrealtime.infrastructure.persistence;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import until.the.eternity.auctionhistory.interfaces.rest.dto.enums.SearchStandard;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.ItemOptionSearchRequest;
import until.the.eternity.auctionhistory.interfaces.rest.dto.request.PriceSearchRequest;
import until.the.eternity.auctionitem.domain.entity.AuctionRealtimeItem;
import until.the.eternity.auctionitem.domain.entity.QAuctionRealtimeItem;
import until.the.eternity.auctionitem.domain.entity.QAuctionRealtimeItemOption;
import until.the.eternity.auctionrealtime.interfaces.rest.dto.request.AuctionRealtimeSearchRequest;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
class AuctionRealtimeQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    /** 옵션 조건 빌드 결과 (조건 BooleanBuilder + 추가된 조건 개수) */
    record OptionConditionResult(BooleanBuilder builder, int count) {}

    /** 실시간 경매장 검색 (옵션 조건 포함) */
    public Page<AuctionRealtimeItem> search(
            AuctionRealtimeSearchRequest condition, Pageable pageable) {
        QAuctionRealtimeItem ar = QAuctionRealtimeItem.auctionRealtimeItem;
        QAuctionRealtimeItemOption aro = QAuctionRealtimeItemOption.auctionRealtimeItemOption;

        // 1단계: 기본 조건 빌드
        BooleanBuilder itemBuilder = buildItemPredicate(condition, ar);

        // 2단계: 옵션 조건이 있으면 서브쿼리 추가
        if (condition.itemOptionSearchRequest() != null) {
            QAuctionRealtimeItemOption subOption = new QAuctionRealtimeItemOption("subOption");
            OptionConditionResult optionResult =
                    buildItemOptionConditions(condition.itemOptionSearchRequest(), subOption);

            if (optionResult.builder().hasValue() && optionResult.count() > 0) {
                var subQuery =
                        JPAExpressions.select(subOption.auctionRealtimeItem.id)
                                .from(subOption)
                                .where(optionResult.builder())
                                .groupBy(subOption.auctionRealtimeItem.id)
                                .having(subOption.count().eq((long) optionResult.count()));

                itemBuilder.and(ar.id.in(subQuery));
            }
        }

        // 3단계: 정렬 조건 빌드
        List<OrderSpecifier<?>> orderSpecifiers = buildOrderSpecifiers(pageable, ar);

        // 4단계: Deferred Join 패턴 적용
        // 4-1단계: ID만 먼저 조회
        List<Long> ids =
                queryFactory
                        .select(ar.id)
                        .from(ar)
                        .where(itemBuilder)
                        .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0L);
        }

        // 4-2단계: ID로 상세 조회
        List<AuctionRealtimeItem> content =
                queryFactory
                        .selectFrom(ar)
                        .leftJoin(ar.auctionRealtimeItemOptions, aro)
                        .fetchJoin()
                        .where(ar.id.in(ids))
                        .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                        .distinct()
                        .fetch();

        // Count 쿼리
        Long total = queryFactory.select(ar.countDistinct()).from(ar).where(itemBuilder).fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    /** 기본 조건 빌드 (카테고리, 아이템명, 가격) */
    private BooleanBuilder buildItemPredicate(
            AuctionRealtimeSearchRequest c, QAuctionRealtimeItem ar) {
        BooleanBuilder builder = new BooleanBuilder();

        if (c.itemTopCategory() != null && !c.itemTopCategory().isBlank()) {
            builder.and(ar.itemTopCategory.eq(c.itemTopCategory()));
        }
        if (c.itemSubCategory() != null && !c.itemSubCategory().isBlank()) {
            builder.and(ar.itemSubCategory.eq(c.itemSubCategory()));
        }
        if (c.itemName() != null && !c.itemName().isBlank()) {
            builder.and(ar.itemName.containsIgnoreCase(c.itemName()));
        }

        if (c.priceSearchRequest() != null) {
            PriceSearchRequest price = c.priceSearchRequest();
            if (price.priceFrom() != null) {
                builder.and(ar.auctionPricePerUnit.goe(price.priceFrom()));
            }
            if (price.priceTo() != null) {
                builder.and(ar.auctionPricePerUnit.loe(price.priceTo()));
            }
        }

        return builder;
    }

    /** 옵션 검색 조건 빌드 (서브쿼리용) */
    private OptionConditionResult buildItemOptionConditions(
            ItemOptionSearchRequest opt, QAuctionRealtimeItemOption aro) {
        BooleanBuilder builder = new BooleanBuilder();
        int conditionCount = 0;
        boolean ergConditionAdded = false;

        // 1. Balance (밸런스)
        if (opt.balanceSearch() != null && opt.balanceSearch().balance() != null) {
            builder.or(
                    buildOptionCondition(
                            aro,
                            "밸런스",
                            opt.balanceSearch().balance(),
                            opt.balanceSearch().balanceStandard()));
            conditionCount++;
        }

        // 2. Critical (크리티컬)
        if (opt.criticalSearch() != null && opt.criticalSearch().critical() != null) {
            builder.or(
                    buildOptionCondition(
                            aro,
                            "크리티컬",
                            opt.criticalSearch().critical(),
                            opt.criticalSearch().criticalStandard()));
            conditionCount++;
        }

        // 3. Defense (방어력)
        if (opt.defenseSearch() != null && opt.defenseSearch().defense() != null) {
            builder.or(
                    buildOptionCondition(
                            aro,
                            "방어력",
                            opt.defenseSearch().defense(),
                            opt.defenseSearch().defenseStandard()));
            conditionCount++;
        }

        // 4. Erg (에르그) - 범위 검색
        if (opt.ergSearch() != null) {
            BooleanExpression ergTypeCondition = aro.optionType.eq("에르그");
            BooleanExpression ergValueCondition = null;

            if (opt.ergSearch().ergFrom() != null && opt.ergSearch().ergTo() != null) {
                ergValueCondition =
                        castOptionValueToInt(aro)
                                .between(opt.ergSearch().ergFrom(), opt.ergSearch().ergTo());
            } else if (opt.ergSearch().ergFrom() != null) {
                ergValueCondition = castOptionValueToInt(aro).goe(opt.ergSearch().ergFrom());
            } else if (opt.ergSearch().ergTo() != null) {
                ergValueCondition = castOptionValueToInt(aro).loe(opt.ergSearch().ergTo());
            }

            if (ergValueCondition != null) {
                BooleanExpression combined = ergTypeCondition.and(ergValueCondition);
                builder.or(Expressions.booleanTemplate("({0})", combined));
                if (!ergConditionAdded) {
                    conditionCount++;
                    ergConditionAdded = true;
                }
            }
        }

        // 5. ErgRank (에르그 등급)
        if (opt.ergRankSearch() != null && opt.ergRankSearch().ergRank() != null) {
            BooleanExpression combined =
                    aro.optionType.eq("에르그").and(aro.optionValue.eq(opt.ergRankSearch().ergRank()));
            builder.or(Expressions.booleanTemplate("({0})", combined));
            if (!ergConditionAdded) {
                conditionCount++;
                ergConditionAdded = true;
            }
        }

        // 6. MagicDefense (마법 방어력)
        if (opt.magicDefenseSearch() != null && opt.magicDefenseSearch().magicDefense() != null) {
            builder.or(
                    buildOptionCondition(
                            aro,
                            "마법 방어력",
                            opt.magicDefenseSearch().magicDefense(),
                            opt.magicDefenseSearch().magicDefenseStandard()));
            conditionCount++;
        }

        // 7. MagicProtect (마법 보호)
        if (opt.magicProtectSearch() != null && opt.magicProtectSearch().magicProtect() != null) {
            builder.or(
                    buildOptionCondition(
                            aro,
                            "마법 보호",
                            opt.magicProtectSearch().magicProtect(),
                            opt.magicProtectSearch().magicProtectStandard()));
            conditionCount++;
        }

        // 8. MaxAttack (공격) - 범위 검색
        if (opt.maxAttackSearch() != null) {
            BooleanExpression attackTypeCondition = aro.optionType.eq("공격");
            BooleanExpression attackValueCondition = null;

            if (opt.maxAttackSearch().maxAttackFrom() != null
                    && opt.maxAttackSearch().maxAttackTo() != null) {
                attackValueCondition =
                        castOptionValueToInt(aro)
                                .between(
                                        opt.maxAttackSearch().maxAttackFrom(),
                                        opt.maxAttackSearch().maxAttackTo());
            } else if (opt.maxAttackSearch().maxAttackFrom() != null) {
                attackValueCondition =
                        castOptionValueToInt(aro).goe(opt.maxAttackSearch().maxAttackFrom());
            } else if (opt.maxAttackSearch().maxAttackTo() != null) {
                attackValueCondition =
                        castOptionValueToInt(aro).loe(opt.maxAttackSearch().maxAttackTo());
            }

            if (attackValueCondition != null) {
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
                            aro,
                            "내구력",
                            opt.maximumDurabilitySearch().maximumDurability(),
                            opt.maximumDurabilitySearch().maximumDurabilityStandard()));
            conditionCount++;
        }

        // 10. MaxInjuryRate (부상률) - 범위 검색
        if (opt.maxInjuryRateSearch() != null) {
            BooleanExpression injuryTypeCondition = aro.optionType.eq("부상률");
            BooleanExpression injuryValueCondition = null;

            if (opt.maxInjuryRateSearch().maxInjuryRateFrom() != null
                    && opt.maxInjuryRateSearch().maxInjuryRateTo() != null) {
                injuryValueCondition =
                        castOptionValueToInt(aro)
                                .between(
                                        opt.maxInjuryRateSearch().maxInjuryRateFrom(),
                                        opt.maxInjuryRateSearch().maxInjuryRateTo());
            } else if (opt.maxInjuryRateSearch().maxInjuryRateFrom() != null) {
                injuryValueCondition =
                        castOptionValueToInt(aro)
                                .goe(opt.maxInjuryRateSearch().maxInjuryRateFrom());
            } else if (opt.maxInjuryRateSearch().maxInjuryRateTo() != null) {
                injuryValueCondition =
                        castOptionValueToInt(aro).loe(opt.maxInjuryRateSearch().maxInjuryRateTo());
            }

            if (injuryValueCondition != null) {
                BooleanExpression combined = injuryTypeCondition.and(injuryValueCondition);
                builder.or(Expressions.booleanTemplate("({0})", combined));
                conditionCount++;
            }
        }

        // 11. Proficiency (숙련)
        if (opt.proficiencySearch() != null && opt.proficiencySearch().proficiency() != null) {
            builder.or(
                    buildOptionCondition(
                            aro,
                            "숙련",
                            opt.proficiencySearch().proficiency(),
                            opt.proficiencySearch().proficiencyStandard()));
            conditionCount++;
        }

        // 12. Protect (보호)
        if (opt.protectSearch() != null && opt.protectSearch().protect() != null) {
            builder.or(
                    buildOptionCondition(
                            aro,
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
                            aro,
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
                            aro,
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
                            aro,
                            "남은 사용 횟수",
                            opt.remainingUseCountSearch().remainingUseCount(),
                            opt.remainingUseCountSearch().remainingUseCountStandard()));
            conditionCount++;
        }

        // 16. WearingRestrictions (착용 제한)
        if (opt.wearingRestrictionsSearch() != null
                && opt.wearingRestrictionsSearch().wearingRestrictions() != null) {
            BooleanExpression condition =
                    aro.optionValue.contains(opt.wearingRestrictionsSearch().wearingRestrictions());
            builder.or(Expressions.booleanTemplate("({0})", condition));
            conditionCount++;
        }

        return new OptionConditionResult(builder, conditionCount);
    }

    /** 옵션 조건 빌드 헬퍼 */
    private BooleanExpression buildOptionCondition(
            QAuctionRealtimeItemOption aro,
            String optionType,
            Integer value,
            SearchStandard standard) {
        BooleanExpression optionTypeCondition = aro.optionType.eq(optionType);
        NumberTemplate<Integer> numValue = castOptionValueToInt(aro);

        BooleanExpression valueCondition;
        if (standard == null || standard.isEqual()) {
            valueCondition = numValue.eq(value);
        } else if (standard.isUp()) {
            valueCondition = numValue.goe(value);
        } else if (standard.isDown()) {
            valueCondition = numValue.loe(value);
        } else {
            valueCondition = numValue.eq(value);
        }

        BooleanExpression combined = optionTypeCondition.and(valueCondition);
        return Expressions.booleanTemplate("({0})", combined);
    }

    /** option_value2 또는 option_value를 Integer로 변환 */
    private NumberTemplate<Integer> castOptionValueToInt(QAuctionRealtimeItemOption aro) {
        return Expressions.numberTemplate(
                Integer.class, "COALESCE({0}, {1}, 0)", aro.optionValue2, aro.optionValue);
    }

    /** Pageable의 Sort를 QueryDSL OrderSpecifier로 변환 */
    private List<OrderSpecifier<?>> buildOrderSpecifiers(
            Pageable pageable, QAuctionRealtimeItem ar) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                String property = order.getProperty();

                OrderSpecifier<?> orderSpecifier =
                        switch (property) {
                            case "dateAuctionExpire" ->
                                    new OrderSpecifier<>(direction, ar.dateAuctionExpire);
                            case "dateRegister" -> new OrderSpecifier<>(direction, ar.dateRegister);
                            case "auctionPricePerUnit" ->
                                    new OrderSpecifier<>(direction, ar.auctionPricePerUnit);
                            case "itemName" -> new OrderSpecifier<>(direction, ar.itemName);
                            default -> new OrderSpecifier<>(Order.ASC, ar.dateAuctionExpire);
                        };

                orders.add(orderSpecifier);
            }
        } else {
            orders.add(new OrderSpecifier<>(Order.ASC, ar.dateAuctionExpire));
        }

        return orders;
    }
}
