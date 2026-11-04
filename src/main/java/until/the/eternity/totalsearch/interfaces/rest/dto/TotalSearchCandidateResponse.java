package until.the.eternity.totalsearch.interfaces.rest.dto;

import until.the.eternity.totalsearch.domain.TotalSearchKeyword;
import until.the.eternity.totalsearch.domain.TotalSearchKeywordType;

public record TotalSearchCandidateResponse(
        TotalSearchKeywordType type, String keyword, String targetKey) {

    public static TotalSearchCandidateResponse from(TotalSearchKeyword keyword) {
        return new TotalSearchCandidateResponse(
                keyword.getType(), keyword.getKeyword(), keyword.getTargetKey());
    }
}
