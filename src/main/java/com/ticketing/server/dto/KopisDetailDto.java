package com.ticketing.server.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.List;

public record KopisDetailDto(
        @JacksonXmlProperty(localName = "prfnm") String title,
        @JacksonXmlProperty(localName = "prfruntime") String runtime,

        // 🌟 KOPIS 가이드북 맞춤: 시간 정보는 dtguidance 입니다!
        @JacksonXmlProperty(localName = "dtguidance") String schedule,

        @JacksonXmlProperty(localName = "sty") String synopsis,
        @JacksonXmlProperty(localName = "prfage") String age,
        @JacksonXmlProperty(localName = "prfcast") String cast,
        @JacksonXmlProperty(localName = "pcseguidance") String priceInfo,

        // 🌟 KOPIS 가이드북 맞춤: 이미지는 <styurls> 안에 배열로 들어옵니다!
        @JacksonXmlProperty(localName = "styurls") StyurlsData styurls
) {
    public record StyurlsData(
            @JacksonXmlElementWrapper(useWrapping = false)
            @JacksonXmlProperty(localName = "styurl")
            List<String> urlList
    ) {}
}