package com.fleemer.web.serialization;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface OperationXmlMixIn {
    @JsonIgnore
    Long getId();
}
