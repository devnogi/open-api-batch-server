package until.the.eternity.auctionhistory.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepositoryPort;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepositoryPort.LatestDateWithIds;
import until.the.eternity.auctionhistory.interfaces.external.dto.OpenApiAuctionHistoryResponse;
import until.the.eternity.common.enums.ItemCategory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuctionHistoryDuplicateCheckerTest {

    @Mock AuctionHistoryRepositoryPort repository;

    @InjectMocks AuctionHistoryDuplicateChecker checker;

    private static final ItemCategory CATEGORY = ItemCategory.SWORD;

    private OpenApiAuctionHistoryResponse dto(String id, Instant dateAuctionBuy) {
        return new OpenApiAuctionHistoryResponse(
                "페러시우스 타이탄 블레이드",
                "신성한 페러시우스 타이탄 블레이드",
                CATEGORY.getSubCategory(),
                1L,
                100L,
                dateAuctionBuy,
                id,
                null);
    }

    @Nested
    @DisplayName("checkDuplicateInBatch 테스트")
    class CheckDuplicateInBatchTest {

        @Test
        @DisplayName("DB에 데이터가 없으면 중복 없음으로 판정")
        void noDuplicateWhenNoDataInDb() {
            // given
            Instant now = Instant.now();
            var batch = List.of(dto("1", now), dto("2", now.minusSeconds(10)));
            when(repository.findLatestDateWithIdsBySubCategory(CATEGORY))
                    .thenReturn(Optional.empty());

            // when
            OptionalInt result = checker.checkDuplicateInBatch(batch, CATEGORY);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 배치이면 중복 없음으로 판정")
        void noDuplicateWhenEmptyBatch() {
            // given
            List<OpenApiAuctionHistoryResponse> batch = List.of();

            // when
            OptionalInt result = checker.checkDuplicateInBatch(batch, CATEGORY);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("모든 데이터가 latestDate 이후면 중복 없음")
        void noDuplicateWhenAllDataAfterLatestDate() {
            // given
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");
            Instant afterLatest = latestDate.plusSeconds(100);
            var batch = List.of(dto("1", afterLatest), dto("2", afterLatest.plusSeconds(10)));

            when(repository.findLatestDateWithIdsBySubCategory(CATEGORY))
                    .thenReturn(
                            Optional.of(new LatestDateWithIds(latestDate, Set.of("existing-1"))));

            // when
            OptionalInt result = checker.checkDuplicateInBatch(batch, CATEGORY);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("중간에 latestDate 이전 데이터가 있으면 해당 인덱스 반환")
        void duplicateFoundWhenDataBeforeLatestDate() {
            // given
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");
            Instant afterLatest = latestDate.plusSeconds(100);
            Instant beforeLatest = latestDate.minusSeconds(100);
            var batch =
                    List.of(dto("1", afterLatest), dto("2", afterLatest), dto("3", beforeLatest));

            when(repository.findLatestDateWithIdsBySubCategory(CATEGORY))
                    .thenReturn(Optional.of(new LatestDateWithIds(latestDate, Set.of())));

            // when
            OptionalInt result = checker.checkDuplicateInBatch(batch, CATEGORY);

            // then
            assertThat(result).hasValue(2);
        }

        @Test
        @DisplayName("동일 날짜, 다른 auctionBuyId는 신규로 판정")
        void sameDateDifferentIdIsNew() {
            // given
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");
            var batch = List.of(dto("new-id-1", latestDate), dto("new-id-2", latestDate));

            when(repository.findLatestDateWithIdsBySubCategory(CATEGORY))
                    .thenReturn(
                            Optional.of(
                                    new LatestDateWithIds(
                                            latestDate, Set.of("existing-1", "existing-2"))));

            // when
            OptionalInt result = checker.checkDuplicateInBatch(batch, CATEGORY);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("동일 날짜, 같은 auctionBuyId는 중복으로 판정")
        void sameDateSameIdIsDuplicate() {
            // given
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");
            var batch = List.of(dto("new-id", latestDate), dto("existing-1", latestDate));

            when(repository.findLatestDateWithIdsBySubCategory(CATEGORY))
                    .thenReturn(
                            Optional.of(
                                    new LatestDateWithIds(
                                            latestDate, Set.of("existing-1", "existing-2"))));

            // when
            OptionalInt result = checker.checkDuplicateInBatch(batch, CATEGORY);

            // then
            assertThat(result).hasValue(1);
        }

        @Test
        @DisplayName("첫 번째 항목이 중복이면 인덱스 0 반환")
        void duplicateAtFirstIndex() {
            // given
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");
            Instant beforeLatest = latestDate.minusSeconds(100);
            var batch = List.of(dto("1", beforeLatest), dto("2", latestDate));

            when(repository.findLatestDateWithIdsBySubCategory(CATEGORY))
                    .thenReturn(Optional.of(new LatestDateWithIds(latestDate, Set.of())));

            // when
            OptionalInt result = checker.checkDuplicateInBatch(batch, CATEGORY);

            // then
            assertThat(result).hasValue(0);
        }
    }

    @Nested
    @DisplayName("filterExisting 테스트")
    class FilterExistingTest {

        @Test
        @DisplayName("DB에 데이터가 없으면 모든 데이터 반환")
        void returnAllWhenNoDataInDb() {
            // given
            Instant now = Instant.now();
            var dtos = List.of(dto("1", now), dto("2", now.minusSeconds(10)));
            when(repository.findLatestDateWithIdsBySubCategory(CATEGORY))
                    .thenReturn(Optional.empty());

            // when
            var result = checker.filterExisting(dtos, CATEGORY);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("빈 리스트이면 빈 리스트 반환")
        void returnEmptyWhenEmptyList() {
            // given
            List<OpenApiAuctionHistoryResponse> dtos = List.of();

            // when
            var result = checker.filterExisting(dtos, CATEGORY);

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
            var dtos =
                    List.of(dto("1", afterLatest), dto("2", beforeLatest), dto("3", afterLatest));

            when(repository.findLatestDateWithIdsBySubCategory(CATEGORY))
                    .thenReturn(Optional.of(new LatestDateWithIds(latestDate, Set.of())));

            // when
            var result = checker.filterExisting(dtos, CATEGORY);

            // then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(OpenApiAuctionHistoryResponse::auctionBuyId)
                    .containsExactly("1", "3");
        }

        @Test
        @DisplayName("동일 날짜지만 기존 ID가 아니면 포함됨")
        void includeSameDateNewId() {
            // given
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");
            var dtos = List.of(dto("new-1", latestDate), dto("new-2", latestDate));

            when(repository.findLatestDateWithIdsBySubCategory(CATEGORY))
                    .thenReturn(
                            Optional.of(new LatestDateWithIds(latestDate, Set.of("existing-1"))));

            // when
            var result = checker.filterExisting(dtos, CATEGORY);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("동일 날짜이고 기존 ID면 필터링됨")
        void filterOutSameDateExistingId() {
            // given
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");
            var dtos =
                    List.of(
                            dto("new-1", latestDate),
                            dto("existing-1", latestDate),
                            dto("new-2", latestDate));

            when(repository.findLatestDateWithIdsBySubCategory(CATEGORY))
                    .thenReturn(
                            Optional.of(new LatestDateWithIds(latestDate, Set.of("existing-1"))));

            // when
            var result = checker.filterExisting(dtos, CATEGORY);

            // then
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(OpenApiAuctionHistoryResponse::auctionBuyId)
                    .containsExactly("new-1", "new-2");
        }

        @Test
        @DisplayName("복합 시나리오: 이전/동일(기존ID)/동일(신규ID)/이후 데이터")
        void complexScenario() {
            // given
            Instant latestDate = Instant.parse("2024-01-01T00:00:00Z");
            Instant afterLatest = latestDate.plusSeconds(100);
            Instant beforeLatest = latestDate.minusSeconds(100);

            var dtos =
                    List.of(
                            dto("after-1", afterLatest), // 신규: 포함
                            dto("before-1", beforeLatest), // 과거: 제외
                            dto("same-new", latestDate), // 동일 날짜, 신규 ID: 포함
                            dto("existing-1", latestDate), // 동일 날짜, 기존 ID: 제외
                            dto("after-2", afterLatest) // 신규: 포함
                            );

            when(repository.findLatestDateWithIdsBySubCategory(CATEGORY))
                    .thenReturn(
                            Optional.of(
                                    new LatestDateWithIds(
                                            latestDate, Set.of("existing-1", "existing-2"))));

            // when
            var result = checker.filterExisting(dtos, CATEGORY);

            // then
            assertThat(result).hasSize(3);
            assertThat(result)
                    .extracting(OpenApiAuctionHistoryResponse::auctionBuyId)
                    .containsExactly("after-1", "same-new", "after-2");
        }
    }
}
