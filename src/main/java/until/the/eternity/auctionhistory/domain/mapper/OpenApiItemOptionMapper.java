package until.the.eternity.auctionhistory.domain.mapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import until.the.eternity.auctionitemoption.domain.dto.external.OpenApiAuctionItemOptionResponse;
import until.the.eternity.auctionitemoption.domain.entity.AuctionHistoryItemOption;

@Mapper(componentModel = "spring")
public interface OpenApiItemOptionMapper {

    String SEGONG_OPTION_TYPE = "세공 옵션";

    // 패턴 1: "스킬명 숫자 레벨" 또는 "스킬명 숫자레벨" 형식 (예: "천옷만들기 품질 보너스 3 레벨", "지력 2레벨")
    // 그룹1: 스킬명, 그룹2: "숫자 레벨" 또는 "숫자레벨" (option_desc), 그룹3: 숫자 (option_value2)
    Pattern PATTERN_LEVEL_SUFFIX = Pattern.compile("^(.+?) ((\\d+) ?레벨)$");

    // 패턴 2: "스킬명(숫자레벨:효과)" 형식 (예: "매그넘 샷 대미지(20레벨:200 % 증가)")
    // 그룹1: 스킬명, 그룹2: 괄호 전체 (option_desc), 그룹3: 숫자 (option_value2)
    Pattern PATTERN_LEVEL_PARENTHESIS = Pattern.compile("^(.+?)(\\((\\d+)레벨:.+\\))$");

    @Mapping(target = "id", ignore = true) // PK 자동 생성
    @Mapping(target = "auctionHistory", ignore = true)
    AuctionHistoryItemOption toEntity(OpenApiAuctionItemOptionResponse itemOption);

    @AfterMapping
    default void afterMapping(
            OpenApiAuctionItemOptionResponse dto, @MappingTarget AuctionHistoryItemOption entity) {
        // option_type이 "세공 옵션"인 경우에만 파싱 수행
        if (!SEGONG_OPTION_TYPE.equals(entity.getOptionType()) || entity.getOptionValue() == null) {
            return;
        }

        String originalValue = entity.getOptionValue();

        // 패턴 1: "스킬명 숫자 레벨" 또는 "스킬명 숫자레벨" 형식
        Matcher matcher1 = PATTERN_LEVEL_SUFFIX.matcher(originalValue);
        if (matcher1.matches()) {
            entity.setOptionValue(matcher1.group(1));
            entity.setOptionValue2(matcher1.group(3));
            entity.setOptionDesc(matcher1.group(2));
            return;
        }

        // 패턴 2: "스킬명(숫자레벨:효과)" 형식
        Matcher matcher2 = PATTERN_LEVEL_PARENTHESIS.matcher(originalValue);
        if (matcher2.matches()) {
            entity.setOptionValue(matcher2.group(1));
            entity.setOptionValue2(matcher2.group(3));
            entity.setOptionDesc(matcher2.group(2));
        }
        // 두 패턴 모두 매칭되지 않으면 원본 값 유지
    }
}
