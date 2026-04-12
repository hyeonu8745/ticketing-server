package com.ticketing.server.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public record KopisDetailDto(
        @JacksonXmlProperty(localName = "prfnm") String title,       // 공연명
        @JacksonXmlProperty(localName = "prfruntime") String runtime, // 공연 시간 (예: 120분)
        @JacksonXmlProperty(localName = "dtls") String schedule,     // 상세 시간표
        @JacksonXmlProperty(localName = "styurl") String detailImage, // 공연 소개 이미지 URL
        @JacksonXmlProperty(localName = "prfpdfrom") String startDate, // 시작일
        @JacksonXmlProperty(localName = "prfpdto") String endDate     // 종료일
) {}