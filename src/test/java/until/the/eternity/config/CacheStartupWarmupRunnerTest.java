package until.the.eternity.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CacheStartupWarmupRunnerTest {

    @Test
    @DisplayName("시작 워밍은 홈 7건과 목록 20건 및 설정값을 사용하고 기본 100건은 제외한다")
    void rankingWarmupLimits_prioritizeHomeLimitWithoutDefaultLimit() {
        assertThat(CacheStartupWarmupRunner.rankingWarmupLimits(50))
                .containsExactly(7, 20, 50)
                .doesNotContain(100);
    }

    @Test
    @DisplayName("시작 워밍 설정값은 최대 랭킹 제한으로 보정한다")
    void rankingWarmupLimits_clampConfiguredLimit() {
        assertThat(CacheStartupWarmupRunner.rankingWarmupLimits(150)).containsExactly(7, 20, 100);
    }
}
