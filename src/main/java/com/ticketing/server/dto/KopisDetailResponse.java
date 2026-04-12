package com.ticketing.server.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "dbs")
public record KopisDetailResponse(
        @JacksonXmlProperty(localName = "db") KopisDetailDto detail
) {}