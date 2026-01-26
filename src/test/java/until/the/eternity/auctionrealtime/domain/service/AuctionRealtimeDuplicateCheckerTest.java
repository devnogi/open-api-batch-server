package until.the.eternity.auctionrealtime.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.auctionrealtime.domain.repository.AuctionRealtimeItemRepositoryPort;
import until.the.eternity.auctionrealtime.domain.service.AuctionRealtimeDuplicateChecker.DuplicateCheckResult;
import until.the.eternity.auctionrealtime.interfaces.external.dto.OpenApiAuctionRealtimeResponse;
import until.the.eternity.common.enums.ItemCategory;

@ExtendWith(MockitoExtension.class)
class AuctionRealtimeDuplicateCheckerTest {

    @Mock AuctionRealtimeItemRepositoryPort repository;

    @InjectMocks AuctionRealtimeDuplicateChecker checker;

    private static final ItemCategory CATEGORY = ItemCategory.SWORD;

    private OpenApiAuctionRealtimeResponse dto(Instant dateAuctionExpire) {
        return new OpenApiAuctionRealtimeResponse(
                "페러시우스 타이탄 블레이드", "신성한 페러시우스 타이탄 블레이드", 1L, 100L, dateAuctionExpire, null);
    }

    @Nested
    @DisplayName("checkDuplicateInBatch 테스트")
    class CheckDuplicateInBatchTest {

        @Test
        @DisplayName("DB에 데이터가 없으면 중복 없음으로 판정")
        void noDuplicateWhenNoDataInDb() {
            // given
            Instant now = Instant.now();
            var batch = List.of(dto(now), dto(now.minusSeconds(10)));
            when(repository.findLatestDateAuctionExpireBySubCategory(CATEGORY))
                    .thenReturn(Optional.empty());

            // when
            DuplicateCheckResult result = checker.checkDuplicateInBatch(batch, CATEGORY);

            // then
            assertThat(result.isDuplicate()).isFalse();
            assertThat(result.hasEqualDate()).isFalse();
            assertThat(result.latestDate()).isNull();
        }

        @Test
        @DisplayName("빈 배치이면 중복 없음으로 판정")
        void noDuplicateWhenEmptyBatch() {
            // given
            List<OpenApiAuctionRealtimeResponse> batch = List.of();

            // when
            DuplicateCheckResult result = checker.checkDuplicateInBatch(batch, CATEGORY);

            // then
            assertThat(result.isDuplicate()).isFalse();
            assertThat(result.hasEqualDate()).isFalse();
        }

        @Test
        @DisplayName("모든 데이터가 latestDate 이후면 중복 없음")
        void noDuplicateWhenAllDataAfterLatestDate() {
            // given
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");
            Instant afterLatest = latestDate.plusSeconds(100);
            var batch = List.of(dto(afterLatest), dto(afterLatest.plusSeconds(10)));

            when(repository.findLatestDateAuctionExpireBySubCategory(CATEGORY))
                    .thenReturn(Optional.of(latestDate));

            // when
            DuplicateCheckResult result = checker.checkDuplicateInBatch(batch, CATEGORY);

            // then
            assertThat(result.isDuplicate()).isFalse();
            assertThat(result.latestDate()).isEqualTo(latestDate);
        }

        @Test
        @DisplayName("중간에 latestDate 이전 데이터가 있으면 해당 인덱스와 중복 반환")
        void duplicateFoundWhenDataBeforeLatestDate() {
            // given
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");
            Instant afterLatest = latestDate.plusSeconds(100);
            Instant beforeLatest = latestDate.minusSeconds(100);
            var batch = List.of(dto(afterLatest), dto(afterLatest), dto(beforeLatest));

            when(repository.findLatestDateAuctionExpireBySubCategory(CATEGORY))
                    .thenReturn(Optional.of(latestDate));

            // when
            DuplicateCheckResult result = checker.checkDuplicateInBatch(batch, CATEGORY);

            // then
            assertThat(result.isDuplicate()).isTrue();
            assertThat(result.hasEqualDate()).isFalse();
            assertThat(result.duplicateIndex()).isEqualTo(2);
            assertThat(result.latestDate()).isEqualTo(latestDate);
        }

        @Test
        @DisplayName("동일 날짜 데이터가 있으면 equalDateFound 반환")
        void equalDateFoundWhenSameDate() {
            // given
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");
            Instant afterLatest = latestDate.plusSeconds(100);
            var batch = List.of(dto(afterLatest), dto(latestDate));

            when(repository.findLatestDateAuctionExpireBySubCategory(CATEGORY))
                    .thenReturn(Optional.of(latestDate));

            // when
            DuplicateCheckResult result = checker.checkDuplicateInBatch(batch, CATEGORY);

            // then
            assertThat(result.isDuplicate()).isTrue();
            assertThat(result.hasEqualDate()).isTrue();
            assertThat(result.duplicateIndex()).isEqualTo(1);
            assertThat(result.latestDate()).isEqualTo(latestDate);
        }

        @Test
        @DisplayName("첫 번째 항목이 과거면 인덱스 0 반환")
        void duplicateAtFirstIndex() {
            // given
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");
            Instant beforeLatest = latestDate.minusSeconds(100);
            var batch = List.of(dto(beforeLatest), dto(latestDate));

            when(repository.findLatestDateAuctionExpireBySubCategory(CATEGORY))
                    .thenReturn(Optional.of(latestDate));

            // when
            DuplicateCheckResult result = checker.checkDuplicateInBatch(batch, CATEGORY);

            // then
            assertThat(result.isDuplicate()).isTrue();
            assertThat(result.duplicateIndex()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("filterForSave 테스트")
    class FilterForSaveTest {

        @Test
        @DisplayName("latestDate가 null이면 모든 데이터 반환")
        void returnAllWhenLatestDateNull() {
            // given
            Instant now = Instant.now();
            var dtos = List.of(dto(now), dto(now.minusSeconds(10)));

            // when
            var result = checker.filterForSave(dtos, null);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("빈 리스트이면 빈 리스트 반환")
        void returnEmptyWhenEmptyList() {
            // given
            List<OpenApiAuctionRealtimeResponse> dtos = List.of();

            // when
            var result = checker.filterForSave(dtos, Instant.now());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("latestDate 이전 데이터는 필터링됨")
        void filterOutDataBeforeLatestDate() {
            // given
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");
            Instant afterLatest = latestDate.plusSeconds(100);
            Instant beforeLatest = latestDate.minusSeconds(100);
            var dtos = List.of(dto(afterLatest), dto(beforeLatest), dto(afterLatest));

            // when
            var result = checker.filterForSave(dtos, latestDate);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("동일 날짜 데이터는 포함됨")
        void includeSameDateData() {
            // given
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");
            var dtos = List.of(dto(latestDate), dto(latestDate));

            // when
            var result = checker.filterForSave(dtos, latestDate);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("복합 시나리오: 이전/동일/이후 데이터")
        void complexScenario() {
            // given
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");
            Instant afterLatest = latestDate.plusSeconds(100);
            Instant beforeLatest = latestDate.minusSeconds(100);

            var dtos =
                    List.of(
                            dto(afterLatest), // 신규: 포함
                            dto(beforeLatest), // 과거: 제외
                            dto(latestDate), // 동일 날짜: 포함
                            dto(afterLatest) // 신규: 포함
                            );

            // when
            var result = checker.filterForSave(dtos, latestDate);

            // then
            assertThat(result).hasSize(3);
        }
    }
}
