package com.fleemer.web.other;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface OperationXmlMixIn {
    @JsonIgnore
    Long getId();
}
