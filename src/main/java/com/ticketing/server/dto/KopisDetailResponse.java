package com.ticketing.server.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public record KopisDetailResponse(
        @JacksonXmlProperty(localName = "db")
        KopisDetailDto detail
) {}