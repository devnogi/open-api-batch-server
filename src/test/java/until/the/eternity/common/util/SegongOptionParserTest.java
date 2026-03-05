package until.the.eternity.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SegongOptionParserTest {

    private static final String SEGONG = "세공 옵션";

    @Nested
    @DisplayName("패턴 1: 스킬명 N 레벨")
    class Pattern1 {

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "풍년가 버프 준비시간 1 레벨",
                    "액스 마스터리 최소 대미지 1 레벨",
                    "애로우 리볼버 최소 대미지 1 레벨",
                    "매그넘 샷 대미지 3 레벨",
                    "마리오네트 마법방어 1 레벨",
                    "의지 1 레벨",
                    "행운 6 레벨",
                    "행운 2 레벨",
                    "솜씨 2 레벨",
                    "보호 3 레벨",
                    "방어 2 레벨",
                    "지력 4 레벨",
                    "최소부상률 5 레벨",
                    "최대마나 3 레벨",
                    "최대 공격력 2 레벨",
                    "크리티컬 1 레벨",
                    "캐스팅 속도 20 레벨",
                    "교역 중 이동 속도 7 레벨",
                    "핸즈 오브 카오스 [변신 중] 부상률 8 레벨",
                    "스피리트 오브 오더 [변신 중] 보호 15 레벨",
                    "스펠 오브 피시스 [변신 중] 최대 마나 10 레벨",
                    "데몬 오브 피시스 [변신 중] 최대 대미지 1 레벨",
                    "잿빛 연막술 방어, 마법방어 감소 수치 3 레벨",
                    "연속기 : 차징 피스트 풀차지 도달 시간 감소 1 레벨",
                    "7막: 광란의 질주 시전시간 3 레벨",
                })
        void shouldParsePattern1(String input) {
            SegongOptionParser.ParseResult result = SegongOptionParser.parse(SEGONG, input);

            assertThat(result).isNotNull();
            assertThat(result.optionValue2()).isNotNull();
            assertThat(result.optionDesc()).isNotNull();
        }
    }

    @Nested
    @DisplayName("패턴 2: 스킬명(N레벨:효과)")
    class Pattern2 {

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "행운(8레벨:12.00 증가)",
                    "행운(1레벨:1.50 증가)",
                    "합성 수련 경험치(8레벨:1.80 배 수련 경험치 증가)",
                    "플레이머 지속 시간(15레벨:3.00 초 증가)",
                    "퓨리 오브 콘누스 [변신 중] 최대 스태미나(2레벨:15.00 증가)",
                    "퓨리 오브 라이트 대미지(19레벨:57 % 증가)",
                    "타운트 유인한 적 1명 당 자신의 방어/마법방어 증가(5레벨:5 증가)",
                    "크리티컬(6레벨:6 % 증가)",
                    "최대생명력(15레벨:37.50 증가)",
                    "윈드밀 대미지(18레벨:54 % 증가)",
                    "데몬 오브 피시스 [변신 중] 최소 대미지(4레벨:4 증가)",
                    "잿빛 연막술 방어, 마법방어 감소 수치(1레벨:1 추가 감소)",
                    "연속기 : 드롭킥 스플래시 대미지(12레벨:12 % 증가)",
                    "7막: 광란의 질주 대미지 배율(17레벨:136 % 증가)",
                    "수리검 폭쇄 적 이동 속도 감소 시간(16레벨:16.00 초 추가 감소)",
                })
        void shouldParsePattern2(String input) {
            SegongOptionParser.ParseResult result = SegongOptionParser.parse(SEGONG, input);

            assertThat(result).isNotNull();
            assertThat(result.optionValue2()).isNotNull();
            assertThat(result.optionDesc()).isNotNull();
        }
    }

    @Nested
    @DisplayName("패턴 2 확장: 스킬명(설명)(N레벨:효과)")
    class Pattern2Extended {

        @ParameterizedTest
        @ValueSource(
                strings = {
                    "교역 중 이동 속도(교역 강화 의상)(19레벨:57 % 증가)",
                    "랜스 차지 범위 폭(자이언트)(19레벨:95.00 cm 증가)",
                })
        void shouldParseDoubleParenthesis(String input) {
            SegongOptionParser.ParseResult result = SegongOptionParser.parse(SEGONG, input);

            assertThat(result).isNotNull();
            assertThat(result.optionValue2()).isNotNull();
            assertThat(result.optionDesc()).isNotNull();
        }
    }

    @Nested
    @DisplayName("패턴 3: 스킬명 설명텍스트")
    class Pattern3 {

        @Test
        void shouldParseDescriptionOnly() {
            SegongOptionParser.ParseResult result =
                    SegongOptionParser.parse(SEGONG, "돌진 인간 및 엘프일 때 방패 없이 사용 가능");

            assertThat(result).isNotNull();
            assertThat(result.optionValue()).isEqualTo("돌진");
            assertThat(result.optionValue2()).isNull();
            assertThat(result.optionDesc()).isEqualTo("인간 및 엘프일 때 방패 없이 사용 가능");
        }
    }

    @Nested
    @DisplayName("세공 옵션이 아닌 경우")
    class NonSegong {

        @Test
        void shouldReturnNullForNonSegongType() {
            assertThat(SegongOptionParser.parse("일반 옵션", "행운 6 레벨")).isNull();
        }

        @Test
        void shouldReturnNullForNullValue() {
            assertThat(SegongOptionParser.parse(SEGONG, null)).isNull();
        }
    }
}
