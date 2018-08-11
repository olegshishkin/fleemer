package com.fleemer.web.form;

import com.fleemer.model.Operation;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OperationForm {
    @Valid
    private Operation operation;

    @Size(max = 255)
    private String inAccountName;

    @Size(max = 255)
    private String outAccountName;

    @Size(max = 255)
    private String categoryName;
}
