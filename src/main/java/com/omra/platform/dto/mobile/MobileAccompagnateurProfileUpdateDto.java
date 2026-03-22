package com.omra.platform.dto.mobile;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MobileAccompagnateurProfileUpdateDto {

    @Size(max = 200)
    private String name;

    @Size(max = 40)
    private String phone;

    @Size(max = 500)
    private String avatar;
}
