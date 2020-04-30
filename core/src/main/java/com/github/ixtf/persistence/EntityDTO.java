package com.github.ixtf.persistence;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author jzb 2019-10-24
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Data
public class EntityDTO implements Serializable {
    @EqualsAndHashCode.Include
    @NotBlank
    private String id;
}
