package com.ticketing.server.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public record KopisEventDto(
        @JacksonXmlProperty(localName = "mt20id") String kopisId,
        @JacksonXmlProperty(localName = "prfnm") String title,
        @JacksonXmlProperty(localName = "prfpdfrom") String startDate,
        @JacksonXmlProperty(localName = "fcltynm") String location,
        @JacksonXmlProperty(localName = "poster") String posterUrl,
        @JacksonXmlProperty(localName = "genrenm") String genre
) {}