package com.fleemer.web.other;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Interface is necessary for ignoring id serialization via Jackson
 */
public interface OperationXmlMixIn {
    @JsonIgnore
    Long getId();
}
