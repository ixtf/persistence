package com.github.ixtf.persistence;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author jzb 2019-10-24
 */
@Data
public class EntityDTO implements Serializable {
    @NotBlank
    private String id;
}
