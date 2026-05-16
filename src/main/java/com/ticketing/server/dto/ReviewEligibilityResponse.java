package com.ticketing.server.dto;

import lombok.Builder;

@Builder
public record ReviewEligibilityResponse(
        boolean eligible,        // 작성 가능 여부
        boolean alreadyWritten,  // 이미 작성했는지
        Long existingReviewId,   // 작성했다면 그 ID
        String reason            // 못 쓰는 경우 사유 (UI 노출용)
) {}
