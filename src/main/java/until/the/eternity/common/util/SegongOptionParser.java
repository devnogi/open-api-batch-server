package until.the.eternity.common.util;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 세공 옵션의 option_value를 파싱하여 스킬명(option_value), 레벨(option_value2), 설명(option_desc)을 분리하는 유틸리티.
 *
 * <p>지원 패턴:
 *
 * <ul>
 *   <li>패턴 1: "스킬명 숫자 레벨" (예: "지력 2레벨", "행운 6 레벨")
 *   <li>패턴 2: "스킬명(숫자레벨:효과)" (예: "매그넘 샷 대미지(20레벨:200 % 증가)")
 *   <li>패턴 2 확장: "스킬명(설명)(숫자레벨:효과)" (예: "교역 중 이동 속도(교역 강화 의상)(19레벨:57 % 증가)")
 *   <li>패턴 3: "스킬명 설명텍스트" (예: "돌진 인간 및 엘프일 때 방패 없이 사용 가능")
 * </ul>
 */
@Slf4j
public final class SegongOptionParser {

    public static final String SEGONG_OPTION_TYPE = "세공 옵션";

    // 패턴 1: "스킬명 숫자 레벨" 또는 "스킬명 숫자레벨" 형식
    // 그룹1: 스킬명, 그룹2: "숫자 레벨" 또는 "숫자레벨" (option_desc), 그룹3: 숫자 (option_value2)
    private static final Pattern PATTERN_LEVEL_SUFFIX = Pattern.compile("^(.+?) ((\\d+) ?레벨)$");

    // 패턴 2: "스킬명(숫자레벨:효과)" 또는 "스킬명(설명)(숫자레벨:효과)" 형식
    // lazy(.+?)가 이중 괄호 케이스에서 첫 번째 괄호까지 확장하여 매칭
    // 그룹1: 스킬명(또는 스킬명+설명괄호), 그룹2: 괄호 전체 (option_desc), 그룹3: 숫자 (option_value2)
    private static final Pattern PATTERN_LEVEL_PARENTHESIS =
            Pattern.compile("^(.+?)(\\((\\d+)레벨:.+\\))$");

    // 패턴 3: "스킬명 설명텍스트" 형식 (레벨 정보 없이 텍스트 설명만 존재)
    // 그룹1: 스킬명, 그룹2: 설명 텍스트 (option_desc)
    private static final Pattern PATTERN_DESCRIPTION_ONLY = Pattern.compile("^(.+?) (.+)$");

    private SegongOptionParser() {}

    public record ParseResult(String optionValue, String optionValue2, String optionDesc) {}

    /**
     * 세공 옵션의 option_value를 파싱하여 스킬명, 레벨, 설명을 분리한다.
     *
     * @param optionType 옵션 타입
     * @param optionValue 원본 옵션 값
     * @return 파싱 결과. 세공 옵션이 아니거나 파싱 불가능한 경우 null
     */
    public static ParseResult parse(String optionType, String optionValue) {
        if (!SEGONG_OPTION_TYPE.equals(optionType) || optionValue == null) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "[SegongOptionParser] Skip parse: optionType='{}', optionValue='{}'",
                        optionType,
                        optionValue);
            }
            return null;
        }

        // 패턴 1: "스킬명 숫자 레벨" 또는 "스킬명 숫자레벨"
        Matcher matcher1 = PATTERN_LEVEL_SUFFIX.matcher(optionValue);
        if (matcher1.matches()) {
            if (log.isDebugEnabled()) {
                log.debug("[SegongOptionParser] Matched LEVEL_SUFFIX: '{}'", optionValue);
            }
            return new ParseResult(matcher1.group(1), matcher1.group(3), matcher1.group(2));
        }

        // 패턴 2: "스킬명(숫자레벨:효과)" 또는 "스킬명(설명)(숫자레벨:효과)"
        Matcher matcher2 = PATTERN_LEVEL_PARENTHESIS.matcher(optionValue);
        if (matcher2.matches()) {
            if (log.isDebugEnabled()) {
                log.debug("[SegongOptionParser] Matched LEVEL_PARENTHESIS: '{}'", optionValue);
            }
            return new ParseResult(matcher2.group(1), matcher2.group(3), matcher2.group(2));
        }

        // 패턴 3: "스킬명 설명텍스트" (레벨 정보 없는 텍스트 설명)
        Matcher matcher3 = PATTERN_DESCRIPTION_ONLY.matcher(optionValue);
        if (matcher3.matches()) {
            if (log.isDebugEnabled()) {
                log.debug("[SegongOptionParser] Matched DESCRIPTION_ONLY: '{}'", optionValue);
            }
            return new ParseResult(matcher3.group(1), null, matcher3.group(2));
        }

        if (log.isDebugEnabled()) {
            log.debug("[SegongOptionParser] No pattern matched for value: '{}'", optionValue);
        }

        return null;
    }
}
