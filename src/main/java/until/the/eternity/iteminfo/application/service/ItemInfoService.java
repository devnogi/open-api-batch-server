package until.the.eternity.iteminfo.application.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import until.the.eternity.auctionhistory.domain.repository.AuctionHistoryRepositoryPort;
import until.the.eternity.batchlog.domain.enums.BatchType;
import until.the.eternity.common.annotation.BatchLog;
import until.the.eternity.config.CacheNames;
import until.the.eternity.iteminfo.domain.entity.ItemInfo;
import until.the.eternity.iteminfo.domain.entity.ItemInfoId;
import until.the.eternity.iteminfo.domain.repository.ItemInfoRepositoryPort;
import until.the.eternity.iteminfo.interfaces.rest.dto.request.ItemInfoSearchRequest;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemCategoryResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoSummaryResponse;
import until.the.eternity.iteminfo.interfaces.rest.dto.response.ItemInfoSyncResponse;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemInfoService {

    private final ItemInfoRepositoryPort itemInfoRepository;
    private final AuctionHistoryRepositoryPort auctionHistoryRepository;

    public List<ItemCategoryResponse> findItemCategories() {
        return ItemCategoryResponse.from();
    }

    @Cacheable(cacheNames = CacheNames.ITEM_INFO_ALL, key = "'all'")
    public List<ItemInfoResponse> findAll() {
        List<ItemInfo> itemInfos = itemInfoRepository.findAll();
        return ItemInfoResponse.from(itemInfos);
    }

    @Cacheable(cacheNames = CacheNames.ITEM_INFO_BY_TOP_CATEGORY, key = "#topCategory")
    public List<ItemInfoResponse> findByTopCategory(String topCategory) {
        List<ItemInfo> itemInfos = itemInfoRepository.findByTopCategory(topCategory);
        return ItemInfoResponse.from(itemInfos);
    }

    @Cacheable(cacheNames = CacheNames.ITEM_INFO_BY_SUB_CATEGORY, key = "#subCategory")
    public List<ItemInfoResponse> findBySubCategory(String subCategory) {
        List<ItemInfo> itemInfos = itemInfoRepository.findBySubCategory(subCategory);
        return ItemInfoResponse.from(itemInfos);
    }

    @Cacheable(
            cacheNames = CacheNames.ITEM_INFO_DETAIL,
            key =
                    "(#searchRequest.name() ?: '') + ':'"
                            + " + (#searchRequest.topCategory() ?: '') + ':'"
                            + " + (#searchRequest.subCategory() ?: '') + ':'"
                            + " + #pageable.pageNumber + ':' + #pageable.pageSize"
                            + " + ':' + #pageable.sort")
    public Page<ItemInfoResponse> findAllDetail(
            ItemInfoSearchRequest searchRequest, Pageable pageable) {
        Page<ItemInfo> itemInfoPage =
                itemInfoRepository.searchWithPagination(searchRequest, pageable);
        return itemInfoPage.map(ItemInfoResponse::from);
    }

    @Cacheable(
            cacheNames = CacheNames.ITEM_INFO_SUMMARY,
            key =
                    "(#searchRequest.name() ?: '') + ':'"
                            + " + (#searchRequest.topCategory() ?: '') + ':'"
                            + " + (#searchRequest.subCategory() ?: '') + ':'"
                            + " + #direction.name()")
    public List<ItemInfoSummaryResponse> findAllSummary(
            ItemInfoSearchRequest searchRequest,
            org.springframework.data.domain.Sort.Direction direction) {
        Pageable pageable =
                org.springframework.data.domain.PageRequest.of(
                        0,
                        Integer.MAX_VALUE,
                        org.springframework.data.domain.Sort.by(direction, "id.name"));
        List<ItemInfo> itemInfos = itemInfoRepository.search(searchRequest, pageable);
        return ItemInfoSummaryResponse.from(itemInfos);
    }

    @Caching(
            evict = {
                @CacheEvict(cacheNames = CacheNames.ITEM_INFO_ALL, allEntries = true),
                @CacheEvict(cacheNames = CacheNames.ITEM_INFO_BY_TOP_CATEGORY, allEntries = true),
                @CacheEvict(cacheNames = CacheNames.ITEM_INFO_BY_SUB_CATEGORY, allEntries = true),
                @CacheEvict(cacheNames = CacheNames.ITEM_INFO_DETAIL, allEntries = true),
                @CacheEvict(cacheNames = CacheNames.ITEM_INFO_SUMMARY, allEntries = true)
            })
    @BatchLog(type = BatchType.ITEM_INFO_SYNC)
    @Transactional
    public ItemInfoSyncResponse syncItemInfoFromAuctionHistory() {
        log.info("Starting to sync ItemInfo from AuctionHistory");

        // 1. AuctionHistory에서 distinct한 아이템 정보 조회
        List<Object[]> distinctItems = auctionHistoryRepository.findDistinctItemInfo();
        log.info("Found {} distinct items in AuctionHistory", distinctItems.size());

        // 2. 기존 ItemInfoId를 한 번의 쿼리로 조회 (N+1 쿼리 문제 해결)
        Set<ItemInfoId> existingIds = new HashSet<>(itemInfoRepository.findAllIds());
        log.info("Found {} existing items in ItemInfo", existingIds.size());

        // 3. 중복되지 않은 아이템만 필터링하여 저장
        List<ItemInfo> newItemInfos = new ArrayList<>();
        List<String> syncedItemNames = new ArrayList<>();

        for (Object[] item : distinctItems) {
            // Query returns: itemName, itemTopCategory, itemSubCategory
            String itemName = (String) item[0];
            String itemTopCategory = (String) item[1];
            String itemSubCategory = (String) item[2];

            ItemInfoId itemInfoId = new ItemInfoId(itemName, itemSubCategory, itemTopCategory);

            // 메모리에서 O(1) 시간 복잡도로 존재 여부 확인
            if (!existingIds.contains(itemInfoId)) {
                ItemInfo itemInfo = ItemInfo.builder().id(itemInfoId).build();

                newItemInfos.add(itemInfo);
                syncedItemNames.add(itemName);
                log.debug("Adding new item to sync: {}", itemName);
            }
        }

        // 4. 새로운 아이템 정보 저장
        if (!newItemInfos.isEmpty()) {
            itemInfoRepository.saveAll(newItemInfos);
            log.info("Successfully synced {} new items to ItemInfo", newItemInfos.size());
        } else {
            log.info("No new items to sync");
        }

        return ItemInfoSyncResponse.of(syncedItemNames);
    }
}
